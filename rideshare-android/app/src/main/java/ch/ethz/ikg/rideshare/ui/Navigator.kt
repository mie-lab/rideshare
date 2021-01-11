package ch.ethz.ikg.rideshare.ui

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.view.MenuItem
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import ch.ethz.ikg.rideshare.R
import ch.ethz.ikg.rideshare.ui.basic.HelpActivity
import ch.ethz.ikg.rideshare.ui.basic.ProfileActivity
import ch.ethz.ikg.rideshare.ui.basic.SettingsActivity
import ch.ethz.ikg.rideshare.ui.login.LoginActivity
import ch.ethz.ikg.rideshare.ui.login.LoginViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class Navigator(
    val activity: Activity,
    private val scope: CoroutineScope,
    private val loginViewModel: LoginViewModel,
    private val navController: NavController
) {

    fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_home -> {
                navController.navigate(R.id.homeFragment)
            }
            R.id.nav_yourtrips -> {
                navController.navigate(R.id.sharedRidesFragment)
            }
            R.id.nav_yourpastmob -> {
                navController.navigate(R.id.pastMobilityFragment)
            }
            R.id.nav_yourpredmob -> {
                navController.navigate(R.id.predictedMobilityFragment)
            }
            R.id.nav_settings -> {
                activity.startActivity(Intent(activity, SettingsActivity::class.java))
            }
            R.id.nav_profile -> {
                activity.startActivity(Intent(activity, ProfileActivity::class.java))
            }
            R.id.nav_logout -> {
                scope.launch {
                    loginViewModel.logout()
                    Handler().postDelayed({
                        val mainIntent = Intent(activity, LoginActivity::class.java)
                        mainIntent.addFlags(
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        )
                        activity.startActivity(mainIntent)
                    }, 0)
                }
            }
            R.id.nav_help -> {
                activity.startActivity(Intent(activity, HelpActivity::class.java))
            }
        }
        val drawerLayout: DrawerLayout = activity.findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}