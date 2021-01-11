package ch.ethz.ikg.rideshare.ui.basic

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import ch.ethz.ikg.rideshare.Constants
import ch.ethz.ikg.rideshare.R
import ch.ethz.ikg.rideshare.services.TrackingService


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings,
                SettingsFragment()
            )
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    fun startTracking() {
        Intent(this, TrackingService::class.java).also { intent ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }
    }

    fun stopTracking() {
        stopService(Intent(this, TrackingService::class.java))
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(Constants.ServiceNotificationId)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val enableTrackingPreference: SwitchPreferenceCompat? = findPreference("enable_tracking")
            enableTrackingPreference?.setOnPreferenceChangeListener { preference, newValue ->
                Log.d(Constants.LogTag, "Preference $preference changed to $newValue.")
                if (newValue as Boolean) {
                    (activity as SettingsActivity).startTracking()
                } else {
                    // Shut down tracking service.
                    (activity as SettingsActivity).stopTracking()
                    Log.d(Constants.LogTag, "Stopping tracking.")
                }
                true
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Respond to the action bar's Up/Home button
            android.R.id.home -> {
                super.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}