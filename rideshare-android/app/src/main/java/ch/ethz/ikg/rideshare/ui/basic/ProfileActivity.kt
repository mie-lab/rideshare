package ch.ethz.ikg.rideshare.ui.basic

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.EditText
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import ch.ethz.ikg.rideshare.Constants
import ch.ethz.ikg.rideshare.R
import ch.ethz.ikg.rideshare.ui.login.LoginViewModel
import ch.ethz.ikg.rideshare.ui.login.LoginViewModelFactory
import ch.ethz.ikg.rideshare.ui.viewmodels.UserViewModel
import ch.ethz.ikg.rideshare.ui.viewmodels.UserViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ProfileActivity : AppCompatActivity() {

    lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Profile"
        supportActionBar?.elevation = 0F

        val email = findViewById<EditText>(R.id.txtin_email)
        val firstName = findViewById<EditText>(R.id.txtin_first_name)
        val lastName = findViewById<EditText>(R.id.txtin_last_name)
        val password = findViewById<EditText>(R.id.txtin_password)
        val repeatPassword = findViewById<EditText>(R.id.txtin_repeat_password)

        userViewModel = ViewModelProviders.of(this, UserViewModelFactory(application))
            .get(UserViewModel::class.java)

        userViewModel.getUser().observe(this@ProfileActivity, Observer {
            email.setText(it.email)
            firstName.setText(it.firstName)
            lastName.setText(it.lastName)
        })

        val fabSave: FloatingActionButton = findViewById(R.id.fab_save)
        fabSave.setOnClickListener {
            Log.d(Constants.LogTag, "Saving profile.")
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
