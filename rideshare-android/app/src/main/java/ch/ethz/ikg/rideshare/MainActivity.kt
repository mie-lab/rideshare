package ch.ethz.ikg.rideshare

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import ch.ethz.ikg.rideshare.services.TrackingService
import ch.ethz.ikg.rideshare.ui.*
import ch.ethz.ikg.rideshare.ui.login.LoginViewModel
import ch.ethz.ikg.rideshare.ui.login.LoginViewModelFactory
import ch.ethz.ikg.rideshare.ui.viewmodels.*
import com.google.android.material.navigation.NavigationView
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import java.util.Calendar
import kotlin.coroutines.CoroutineContext


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    CoroutineScope,
    HomeFragment.OnFragmentInteractionListener,
    PastMobilityFragment.OnFragmentInteractionListener,
    PredictedMobilityFragment.OnFragmentInteractionListener,
    SharedRidesFragment.OnFragmentInteractionListener {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + SupervisorJob()

    private lateinit var sharedPreferences: SharedPreferences

    lateinit var trackingDataViewModel: TrackingDataViewModel
    lateinit var loginViewModel: LoginViewModel
    lateinit var userViewModel: UserViewModel
    lateinit var appStateViewModel: AppStateViewModel

    private lateinit var navigator: Navigator


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        trackingDataViewModel =
            ViewModelProviders.of(this, TrackingDataViewModelFactory(application))
                .get(TrackingDataViewModel::class.java)
        loginViewModel = ViewModelProviders.of(this, LoginViewModelFactory(application))
            .get(LoginViewModel::class.java)
        userViewModel = ViewModelProviders.of(this, UserViewModelFactory(application))
            .get(UserViewModel::class.java)
        appStateViewModel = ViewModelProviders.of(this).get(AppStateViewModel::class.java)
        appStateViewModel.currentDate.value = Calendar.getInstance()

        navigator =
            Navigator(this, this, loginViewModel, this.findNavController(R.id.nav_host_fragment))

        setUpUI()
        setUpNotificationChannel()
        askForPermissions()
    }

    /**
     * Asks for necessary permissions for the app to work.
     */
    private fun askForPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 51
            )
        } else {
            setUpTrackingService()
        }
    }

    /**
     * Sets up everything that is related to the user interface.
     */
    private fun setUpUI() {
        // Setting up toolbar and navigation.
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navView.setNavigationItemSelectedListener(this)
        navView.menu.getItem(0).isChecked = true

        val navHeader = navView.getHeaderView(0)
        val navUserImage: CircleImageView = navHeader.findViewById(R.id.profile_image)
        val navName: TextView = navHeader.findViewById(R.id.txt_nav_name)
        val navEmail: TextView = navHeader.findViewById(R.id.txt_nav_email)
        userViewModel.getUser().observe(this@MainActivity, Observer {
            if (it != null) {
                navName.text = it.firstName.capitalize() + " " + it.lastName.capitalize()
                navEmail.text = it.email
            }
        })

        // Button to test notifications.
        /*val btnNotification: Button = findViewById(R.id.btnNotification)
        btnNotification.setOnClickListener { view ->
            val snoozeIntent = Intent(this, NotificationReceiver::class.java).apply {
                action = "ACTION_SNOOZE"
                putExtra(EXTRA_NOTIFICATION_ID, 0)
            }

            val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
            val snoozePendingIntent: PendingIntent =
                PendingIntent.getBroadcast(this, 0, snoozeIntent, 0)

            val builder = NotificationCompat.Builder(this, Constants.NotificationChannelId)
                .setSmallIcon(R.drawable.navigation_empty_icon)
                .setContentTitle("My notification")
                .setContentText("Hello World!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .addAction(
                    R.drawable.ic_menu_send, getString(R.string.search_menu_title),
                    snoozePendingIntent
                )
            with(NotificationManagerCompat.from(this)) {
                // notificationId is a unique int for each notification that you must define
                notify(Constants.NotificationId, builder.build())
            }
        }*/
    }

    /**
     *
     */
    private fun setUpNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Rideshare notification channel"
            val descriptionText = "This would be the description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel =
                NotificationChannel(Constants.NotificationChannelId, name, importance).apply {
                    description = descriptionText
                }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Sets up everything that is related to the tracking service.
     */
    private fun setUpTrackingService() {
        if (sharedPreferences.getBoolean("enable_tracking", true)) {
            Intent(this, TrackingService::class.java).also { intent ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
            }
        }
    }

    override fun onFragmentInteraction(uri: Uri) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * What to do when the user grants or rejects a requested permission.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            51 -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay!
                    setUpTrackingService()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(
                        this,
                        "Cannot track without permission. Please reactivate in settings.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return
            }
        }
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (sharedPreferences.getBoolean("developer_mode", false)) {
            menuInflater.inflate(R.menu.main, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_clear_local_db -> {
                trackingDataViewModel.deleteAllTrackpoints()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        navigator.onNavigationItemSelected(item)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext[Job]!!.cancel()
    }
}
