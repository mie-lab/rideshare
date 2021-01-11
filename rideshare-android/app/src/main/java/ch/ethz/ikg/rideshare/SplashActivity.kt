package ch.ethz.ikg.rideshare

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import ch.ethz.ikg.rideshare.ui.login.LoginActivity
import ch.ethz.ikg.rideshare.ui.login.LoginViewModel
import ch.ethz.ikg.rideshare.ui.login.LoginViewModelFactory
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


class SplashActivity : AppCompatActivity(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + SupervisorJob()

    private var userViewModel: LoginViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        userViewModel = ViewModelProviders.of(this, LoginViewModelFactory(application))
            .get(LoginViewModel::class.java)

        // Give the app some time to initialize things.
        Handler().postDelayed({
            this.launch {
                // Try to log in by first getting the token ...
                if (userViewModel?.isLoggedIn() == true) {
                    // ... then to make a request to see if the token is still valid ...
                    // TODO Is token still valid?

                    // ... and if so navigate to the main activity.
                    goTo(MainActivity::class.java)
                } else {
                    // Otherwise, navigate to the login activity.
                    goTo(LoginActivity::class.java)
                }
            }
        }, 500)
    }

    private fun <T> goTo(cls: Class<T>) {
        Handler().postDelayed({
            val mainIntent = Intent(this, cls)
            this.startActivity(mainIntent)
            this.finish()
        }, Constants.SplashDisplayLength - 500)
    }
}
