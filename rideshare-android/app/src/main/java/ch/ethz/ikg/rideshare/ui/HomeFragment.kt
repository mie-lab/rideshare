package ch.ethz.ikg.rideshare.ui

import android.content.Context
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import ch.ethz.ikg.rideshare.Constants
import ch.ethz.ikg.rideshare.MainActivity
import ch.ethz.ikg.rideshare.R
import ch.ethz.ikg.rideshare.util.ColorMapping
import ch.ethz.ikg.rideshare.util.Coroutines
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import java.util.*
import kotlin.math.roundToInt


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [HomeFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment(), OnMapReadyCallback {
    private var listener: OnFragmentInteractionListener? = null
    private lateinit var mainActivity: MainActivity

    private var map: GoogleMap? = null
    private lateinit var dailyMobilityChart: PieChart
    private lateinit var distByMode: TextView
    private lateinit var distTot: TextView
    private lateinit var txtCurrentDate: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainActivity = activity as MainActivity
        dailyMobilityChart = view.findViewById(R.id.chart1)
        distByMode = view.findViewById(R.id.txt_home_dist_per_mode)
        distTot = view.findViewById(R.id.txt_home_tot_dist)
        txtCurrentDate = view.findViewById(R.id.txt_current_date)

        setUpUI(view)
        setUpMap(view)
    }

    /**
     * Sets up everything that is related to the user interface.
     */
    private fun setUpUI(view: View) {
        // Setting up toolbar and navigation.
        mainActivity.trackingDataViewModel.getAllNonSyncedTrackpoints()
            .observe(mainActivity, Observer {
                updateMap()
                updateChart()
            })
        mainActivity.appStateViewModel.currentDate.observe(mainActivity, Observer {
            updateMap()
            updateChart()
        })

        val btnPrevDate: ImageButton = view.findViewById(R.id.btnPrevDate)
        btnPrevDate.setOnClickListener {
            mainActivity.appStateViewModel.currentDate.value?.let {
                val dateMinusOne = it
                dateMinusOne.add(Calendar.DATE, -1)
                mainActivity.appStateViewModel.currentDate.value = dateMinusOne
            }
        }

        val btnNextDate: ImageButton = view.findViewById(R.id.btnNextDate)
        btnNextDate.setOnClickListener {
            mainActivity.appStateViewModel.currentDate.value?.let {
                val dateMinusOne = it
                dateMinusOne.add(Calendar.DATE, 1)
                mainActivity.appStateViewModel.currentDate.value = dateMinusOne
            }
        }

        val toolbar: Toolbar = mainActivity.findViewById(R.id.toolbar)
        mainActivity.userViewModel.getUser().observe(mainActivity, Observer {
            if (it != null) {
                toolbar.title = "Hi, " + it.firstName.capitalize()
            }
        })

        dailyMobilityChart.setUsePercentValues(true)
        dailyMobilityChart.description.isEnabled = false
        dailyMobilityChart.legend.isEnabled = false
        dailyMobilityChart.setDrawEntryLabels(false)

        dailyMobilityChart.dragDecelerationFrictionCoef = 0.95f

        dailyMobilityChart.isDrawHoleEnabled = true
        dailyMobilityChart.setHoleColor(Color.WHITE)

        dailyMobilityChart.setTransparentCircleColor(Color.WHITE)
        dailyMobilityChart.setTransparentCircleAlpha(110)

        dailyMobilityChart.holeRadius = 70f
        dailyMobilityChart.transparentCircleRadius = 180f

        // add a selection listener
        dailyMobilityChart.animateY(1400, Easing.EaseInOutQuad)

        val data = PieDataSet(
            listOf(PieEntry(52f, "Car"), PieEntry(30f, "Train"), PieEntry(18f, "Walk")),
            "Transport Modes"
        )
        data.setDrawValues(false)
        data.setDrawIcons(false)
        data.sliceSpace = 3f
        dailyMobilityChart.data = PieData(data)
        dailyMobilityChart.invalidate()
    }

    /**
     * Sets up everything that is related to the map.
     */
    private fun setUpMap(view: View) {
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * What needs to be done when the map is ready.
     */
    override fun onMapReady(readyMap: GoogleMap) {
        map = readyMap
        val sydney = LatLng(47.5, 8.5)
        map?.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        map?.moveCamera(CameraUpdateFactory.zoomTo(15f))

        val googleLogo =
            view!!.findViewById<ViewGroup>(R.id.mapView).findViewWithTag<View>("GoogleWatermark")

        // Reposition Google Logo a bit.
        val glLayoutParams = googleLogo.layoutParams as RelativeLayout.LayoutParams
        glLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        glLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE)
        glLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START, 0)
        glLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
        glLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, 0)
        glLayoutParams.setMargins(32, 0, 0, 490)
        googleLogo.layoutParams = glLayoutParams
    }

    private fun updateMap() {
        map?.let { map ->
            map.clear()
            Coroutines.ioThenMain({
                val trackpointsOnDate = mainActivity.trackingDataViewModel
                    .getAllTrackpointsOnDate(mainActivity.appStateViewModel.currentDate.value!!.time)
                val triplegsOnDate = mainActivity.trackingDataViewModel
                    .getAllTriplegsOnDate(mainActivity.appStateViewModel.currentDate.value!!.time)
                val staypointsOnDate = mainActivity.trackingDataViewModel
                    .getAllStaypointsOnDate(mainActivity.appStateViewModel.currentDate.value!!.time)
                Triple(trackpointsOnDate, triplegsOnDate, staypointsOnDate)
            }, { res ->
                val trackpoints = res!!.first
                val triplegs = res.second
                val staypoints = res.third

                val timeOfLastTripleg = triplegs.map { it.finishedAt }.max()
                val trackpointsToDraw =
                    trackpoints.filter { it.timestamp > timeOfLastTripleg ?: 0 }
                val lastTrackpoint = trackpointsToDraw.maxBy { it.timestamp }

                Log.d(Constants.LogTag, lastTrackpoint?.timestamp.toString())
                // We take a maximum of 350 points as otherwise the app won't handle it anymore.
                for (trackpoint in trackpointsToDraw.orEmpty().take(500)) {
                    map.addCircle(
                        CircleOptions().center(LatLng(trackpoint.latitude, trackpoint.longitude))
                            .radius(17.5)
                            .fillColor(
                                ContextCompat.getColor(
                                    mainActivity.applicationContext,
                                    R.color.circleColor
                                )
                            )
                            .strokeColor(Color.TRANSPARENT)
                    )
                }

                // Move the camera to the last trackpoint.
                lastTrackpoint?.let {
                    val newLocation = LatLng(it.latitude, it.longitude)
                    map.moveCamera(CameraUpdateFactory.newLatLng(newLocation))
                }

                for (tripleg in triplegs.orEmpty()) {
                    val latLngs = mutableListOf<LatLng>()

                    for (coord in tripleg.geom) {
                        latLngs.add(0, LatLng(coord.second, coord.first))
                    }

                    val optsFront = PolylineOptions().add(*latLngs.reversed().toTypedArray())
                        .width(5f).color(
                            ColorMapping.getColor(
                                mainActivity.applicationContext,
                                tripleg.modeValidated
                            )
                        )
                    // map.addPolyline(optsBack)
                    map.addPolyline(optsFront)
                }

                for (staypoint in staypoints.orEmpty()) {
                    map.addCircle(
                        CircleOptions().center(LatLng(staypoint.latitude, staypoint.longitude))
                            .radius(35.0)
                            .fillColor(
                                ContextCompat.getColor(
                                    mainActivity.applicationContext,
                                    R.color.polylineBack
                                )
                            )
                            .strokeColor(Color.TRANSPARENT)
                    )
                }
            })
        }
    }

    fun updateChart() {
        Coroutines.ioThenMain({
            mainActivity.trackingDataViewModel
                .getAllTriplegsOnDate(mainActivity.appStateViewModel.currentDate.value!!.time)
        }, { res ->
            // Get top three in terms of distance.
            val triplegs = res!!
            val distOfTriplegs = triplegs.groupBy { it.modeValidated }
                .map { (mode, tls) ->
                    mode to tls.foldRight(0.0, { tl, acc -> acc + tl.length })
                }
            val topThreeTriplegs = distOfTriplegs
                .sortedBy { -it.second }
                .take(3)
            val totDist = distOfTriplegs.foldRight(0.0, { el, acc -> acc + el.second })

            // Update Pie Chart.
            val data = PieDataSet(
                topThreeTriplegs
                    .map { (mode, dist) -> PieEntry(dist.toFloat(), mode.capitalize()) },
                "Transport Modes"
            )
            data.colors = topThreeTriplegs.map { (mode, _) ->
                ColorMapping.getColor(
                    mainActivity,
                    mode
                )
            }
            data.setDrawValues(false)
            data.setDrawIcons(false)
            data.sliceSpace = 3f
            dailyMobilityChart.data = PieData(data)
            dailyMobilityChart.invalidate()

            // Update top-3 fields and total.
            distByMode.text =
                topThreeTriplegs.map { (mode, dist) -> dist.roundToInt().toString() + " km " + mode.capitalize() }
                    .joinToString(separator = "\n")
            distTot.text = totDist.roundToInt().toString() + " km Total"

            val pattern = "EEEE, d MMMM yyyy"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val primaryLocale = resources.configuration.locales.get(0)
                val dateFormat = SimpleDateFormat(pattern, primaryLocale)
                txtCurrentDate.text =
                    dateFormat.format(mainActivity.appStateViewModel.currentDate.value!!.time)
            } else {
                txtCurrentDate.text =
                    mainActivity.appStateViewModel.currentDate.value!!.get(Calendar.DAY_OF_MONTH).toString() +
                            "." + mainActivity.appStateViewModel.currentDate.value!!.get(Calendar.MONTH) +
                            "." + mainActivity.appStateViewModel.currentDate.value!!.get(Calendar.YEAR)
            }
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}
