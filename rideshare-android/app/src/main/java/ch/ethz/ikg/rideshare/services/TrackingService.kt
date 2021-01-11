package ch.ethz.ikg.rideshare.services

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import ch.ethz.ikg.rideshare.Constants
import ch.ethz.ikg.rideshare.MainActivity
import ch.ethz.ikg.rideshare.R
import ch.ethz.ikg.rideshare.comm.ServerComm
import ch.ethz.ikg.rideshare.data.model.Staypoint
import ch.ethz.ikg.rideshare.data.model.Trackpoint
import ch.ethz.ikg.rideshare.data.model.Tripleg
import ch.ethz.ikg.rideshare.data.repos.TrackingDataRepository
import ch.ethz.ikg.rideshare.data.repos.UserRepository
import ch.ethz.ikg.rideshare.util.Coroutines
import ch.ethz.ikg.rideshare.util.Result
import com.beust.klaxon.Klaxon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext


class TrackingService : LifecycleService(), CoroutineScope, LocationListener {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + SupervisorJob()

    private var locationManager: LocationManager? = null

    private lateinit var trackingDataRepository: TrackingDataRepository
    private lateinit var userRepository: UserRepository

    private var lastSent = 0L
    private var isUploadingData: AtomicBoolean = AtomicBoolean(false)

    override fun onCreate() {
        super.onCreate()
        Log.d(Constants.LogTag, "Created tracking service.")

        userRepository = UserRepository(application)
        trackingDataRepository = TrackingDataRepository(application, userRepository)

        trackingDataRepository.getNonSynchronized().observe(this, Observer {
            trySendToServer(it)
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        // Make sure this foreground service doesn't get killed right away (by popping up a notification).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val builder = Notification.Builder(this, Constants.NotificationChannelId)
                .setContentTitle("SCCER Rideshare is recording your location")
                .setContentText("Tap to open app.")
                .setSmallIcon(R.drawable.logo_small_64px)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
            val notification = builder.build()
            startForeground(Constants.ServiceNotificationId, notification)
            Log.i(Constants.LogTag, "Starting service in foreground (O+)")
        } else {
            val builder = NotificationCompat.Builder(this, Constants.NotificationChannelId)
                .setContentTitle("SCCER Rideshare is recording your location")
                .setContentText("Tap to open app.")
                .setSmallIcon(R.drawable.logo_small_64px)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
            val notification = builder.build()
            startForeground(Constants.ServiceNotificationId, notification)
            Log.i(Constants.LogTag, "Starting service in foreground")
        }

        // Request the location manager.
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return Service.START_REDELIVER_INTENT
        } else {
            Log.i(Constants.LogTag, "Starting LocationManager")
            locationManager?.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, Constants.minLocationUpdateTimeNetwork,
                Constants.minLocationDistanceNetwork, this
            )
            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, Constants.minLocationUpdateTimeGPS,
                Constants.minLocationDistanceGPS, this
            )
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        TODO("Return the communication channel to the service.")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v(Constants.LogTag, "Destroying service")
        locationManager?.removeUpdates(this)
        coroutineContext[Job]!!.cancel()
        stopForeground(true)
    }

    /**
     * Tries to upload all trackpoints to the server.
     */
    private fun trySendToServer(pts: List<Trackpoint>) {
        // We only send to the server once every 15 minutes.
        if (!isUploadingData.get() && System.currentTimeMillis() > lastSent + Constants.ServerUpdateTime) {
            Coroutines.ioThenMain({
                sendToServer(pts)
                lastSent = System.currentTimeMillis()
                val results = getTriplegsAndStaypointsFromServer(Calendar.getInstance().time)
                val triplegs = results.first.toTypedArray()
                val staypoints = results.second.toTypedArray()
                // Remove old triplegs and replace with new ones.
                trackingDataRepository.replaceAllTriplegs(*triplegs)
                trackingDataRepository.replaceAllStaypoints(*staypoints)
            }, {

            })
        }
    }

    /**
     * Sends trackpoints to the server and handles response.
     */
    private fun sendToServer(pts: List<Trackpoint>) {
        Log.d(Constants.LogTag, "Uploading trackpoints ...")
        isUploadingData.set(true)
        when (val resp = ServerComm.postServer(
            Constants.ServerURL + "trackpoints/", Klaxon().toJsonString(pts), userRepository
        )) {
            is Result.Success -> {
                // TODO If a new trackpoint is recorded between synchronization and this call, then it
                // doesn't get added anymore.
                Log.d(Constants.LogTag, "Uploaded trackpoints successfully.")
                trackingDataRepository.setAllSynchronized(pts)
                isUploadingData.set(false)
            }
            is Result.Error -> {
                Log.e(Constants.LogTag, "Error sending trackpoints to server.", resp.exception)
                isUploadingData.set(false)
            }
        }
    }

    private fun getTriplegsAndStaypointsFromServer(date: Date): Pair<List<Tripleg>, List<Staypoint>> {
        Log.d(Constants.LogTag, "Retrieving triplegs ...")
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.UK)
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.UK)

        return when (val result = ServerComm.getServer(
            Constants.ServerURL + "process-trackpoints/" + df.format(date) + "/", userRepository
        )) {
            is Result.Success -> {
                val triplegsJson = result.data.getJSONArray("triplegs")
                val staypointsJson = result.data.getJSONArray("staypoints")

                val triplegs: MutableList<Tripleg> = mutableListOf()
                for (tripleg in MutableList(triplegsJson.length(), triplegsJson::getJSONObject)) {

                    val geom: MutableList<Pair<Double, Double>> = mutableListOf()
                    val coordJson = tripleg.getJSONObject("geom").getJSONArray("coords")
                    for (coord in MutableList(coordJson.length(), coordJson::getJSONArray)) {
                        geom.add(Pair(coord.getDouble(0), coord.getDouble(1)))
                    }

                    val startedAt = GregorianCalendar.getInstance()
                    startedAt.time =
                        sdf.parse(tripleg.getJSONObject("started_at").getString("_isoformat"))
                    val finishedAt = GregorianCalendar.getInstance()
                    finishedAt.time =
                        sdf.parse(tripleg.getJSONObject("finished_at").getString("_isoformat"))

                    val modeValidated = tripleg.getString("mode_validated")

                    triplegs.add(
                        Tripleg(
                            0, startedAt.timeInMillis, finishedAt.timeInMillis, modeValidated, geom
                        )
                    )
                }

                val staypoints: MutableList<Staypoint> = mutableListOf()
                for (staypoint in MutableList(
                    staypointsJson.length(),
                    staypointsJson::getJSONObject
                )) {

                    val startedAt = GregorianCalendar.getInstance()
                    startedAt.time =
                        sdf.parse(staypoint.getJSONObject("started_at").getString("_isoformat"))
                    val finishedAt = GregorianCalendar.getInstance()
                    finishedAt.time =
                        sdf.parse(staypoint.getJSONObject("finished_at").getString("_isoformat"))

                    staypoints.add(
                        Staypoint(
                            0, startedAt.timeInMillis, finishedAt.timeInMillis,
                            staypoint.getJSONObject("geom").getJSONArray("coords").getDouble(0),
                            staypoint.getJSONObject("geom").getJSONArray("coords").getDouble(1)
                        )
                    )
                }

                Log.d(Constants.LogTag, "Retrieved triplegs successfully.")
                Pair(triplegs, staypoints)
            }
            is Result.Error -> {
                Log.d(Constants.LogTag, "Error getting triplegs from server.")
                Pair(listOf(), listOf())
            }
        }
    }

    override fun onLocationChanged(loc: Location) {
        Log.i(
            Constants.LogTag,
            "Location changed to ${loc.longitude}, ${loc.latitude} (${loc.provider})."
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            trackingDataRepository.storeTrackpoint(
                System.currentTimeMillis(),
                loc.longitude,
                loc.latitude,
                loc.accuracy,
                loc.altitude,
                loc.verticalAccuracyMeters,
                loc.bearing,
                loc.bearingAccuracyDegrees,
                loc.provider,
                loc.speed,
                loc.speedAccuracyMetersPerSecond
            )
        } else {
            trackingDataRepository.storeTrackpoint(
                System.currentTimeMillis(), loc.longitude, loc.latitude, loc.accuracy,
                loc.altitude, null, loc.bearing, null, loc.provider,
                loc.speed, null
            )
        }
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {

    }

    override fun onProviderDisabled(provider: String) {
        //Toast.makeText(owner, "Tracking device disabled ($provider)", Toast.LENGTH_SHORT).show()
    }


    override fun onProviderEnabled(provider: String) {
        //Toast.makeText(this, "Tracking device enabled ($provider)", Toast.LENGTH_SHORT).show()
    }
}
