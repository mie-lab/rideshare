package ch.ethz.ikg.rideshare.data.repos

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ch.ethz.ikg.rideshare.Constants
import ch.ethz.ikg.rideshare.comm.ServerComm
import ch.ethz.ikg.rideshare.data.AppDatabase
import ch.ethz.ikg.rideshare.data.daos.StaypointDao
import ch.ethz.ikg.rideshare.data.daos.TrackpointDao
import ch.ethz.ikg.rideshare.data.daos.TriplegDao
import ch.ethz.ikg.rideshare.data.model.Staypoint
import ch.ethz.ikg.rideshare.data.model.Trackpoint
import ch.ethz.ikg.rideshare.data.model.Tripleg
import ch.ethz.ikg.rideshare.util.Coroutines
import ch.ethz.ikg.rideshare.util.Result
import com.beust.klaxon.Klaxon
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.*

class TrackingDataRepository(application: Application, private val userRepository: UserRepository) {
    private var trackpointDao: TrackpointDao
    private var triplegDao: TriplegDao
    private var staypointDao: StaypointDao

    private var allTrackpoints: LiveData<List<Trackpoint>> = MutableLiveData()
    private var allNonSyncTrackpoints: LiveData<List<Trackpoint>> = MutableLiveData()

    private var allTriplegs: LiveData<List<Tripleg>> = MutableLiveData()
    private var allStaypoints: LiveData<List<Staypoint>> = MutableLiveData()

    init {
        val database: AppDatabase = AppDatabase.getInstance(
            application.applicationContext
        )!!

        trackpointDao = database.trackpointDao()
        allTrackpoints = trackpointDao.getAll()
        allTrackpoints.observeForever { }
        allNonSyncTrackpoints = trackpointDao.getNonSynchronized()
        allNonSyncTrackpoints.observeForever { }

        triplegDao = database.triplegDao()
        allTriplegs = triplegDao.getAll()
        allTriplegs.observeForever { }

        staypointDao = database.staypointDao()
        allStaypoints = staypointDao.getAll()
        allStaypoints.observeForever { }
    }

    fun getStartAndEndOfDay(date: Date): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return Pair(calendar.timeInMillis, calendar.timeInMillis + (24 * 60 * 60 * 1000))
    }

    fun getAll(): LiveData<List<Trackpoint>> {
        return allTrackpoints
    }

    fun getAllOnDate(date: Date): List<Trackpoint> {
        val (start, end) = getStartAndEndOfDay(date)
        return trackpointDao.getAllOnDate(start, end)
    }

    fun getNonSynchronized(): LiveData<List<Trackpoint>> {
        return allNonSyncTrackpoints
    }

    fun insertAll(vararg trackpoints: Trackpoint) {
        Coroutines.io {
            trackpointDao.insertAll(*trackpoints)
        }
    }

    fun setAllSynchronized(pts: List<Trackpoint>) {
        Coroutines.io {
            val ptIds = pts.map { it.uid }
            Log.d(Constants.LogTag, "Setting $ptIds to synced.")
            trackpointDao.setAllSynchronized(ptIds)
        }
    }

    fun deleteAll() {
        Coroutines.io {
            trackpointDao.deleteAll()
        }
    }

    fun getAllTriplegs(): LiveData<List<Tripleg>> {
        return allTriplegs
    }

    fun getAllTriplegsOnDate(date: Date): List<Tripleg> {
        val (start, end) = getStartAndEndOfDay(date)
        return triplegDao.getAllOnDate(start, end)
    }

    fun replaceAllTriplegs(vararg triplegs: Tripleg) {
        Coroutines.io {
            triplegDao.replaceAll(*triplegs)
        }
    }

    fun getAllStaypoints(): LiveData<List<Staypoint>> {
        return allStaypoints
    }

    fun getAllStaypointsOnDate(date: Date): List<Staypoint> {
        val (start, end) = getStartAndEndOfDay(date)
        return staypointDao.getAllOnDate(start, end)
    }

    fun replaceAllStaypoints(vararg staypoints: Staypoint) {
        Coroutines.io {
            staypointDao.replaceAll(*staypoints)
        }
    }

    /**
     * Stores a new trackpoint to the local database.
     */
    fun storeTrackpoint(
        time: Long, lon: Double, lat: Double, accuracy: Float, elevation: Double,
        verticalAccuracy: Float?, bearing: Float, bearingAccuracy: Float?,
        provider: String, speed: Float, speedAccuracy: Float?
    ) {

        Coroutines.io {
            val trackpoint = Trackpoint(
                0, false, time, lon, lat,
                accuracy, elevation, verticalAccuracy, bearing, bearingAccuracy, provider,
                speed, speedAccuracy
            )
            insertAll(trackpoint)
            Log.d(Constants.LogTag, "Inserted new trackpoint: $trackpoint")
        }
    }
}