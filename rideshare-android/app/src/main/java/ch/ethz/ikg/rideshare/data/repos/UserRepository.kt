package ch.ethz.ikg.rideshare.data.repos

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ch.ethz.ikg.rideshare.Constants
import ch.ethz.ikg.rideshare.comm.ServerComm
import ch.ethz.ikg.rideshare.data.AppDatabase
import ch.ethz.ikg.rideshare.data.daos.UserDao
import ch.ethz.ikg.rideshare.data.model.User
import ch.ethz.ikg.rideshare.util.Coroutines
import ch.ethz.ikg.rideshare.util.Result
import org.json.JSONObject
import java.io.IOException

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-database cache of login status and user credentials information.
 *
 * TODO Think: Do we need to encrypt the user password?
 */

class UserRepository(application: Application) {
    private var userDao: UserDao

    // in-memory cache of the loggedInUser object
    var user: LiveData<User> = MutableLiveData()
        private set

    val isLoggedIn: Boolean
        get() = user.value != null

    init {
        val database: AppDatabase = AppDatabase.getInstance(
            application.applicationContext
        )!!

        userDao = database.userDao()
        user = userDao.getUser()
        // We need this as otherwise Room won't trigger an update :S.
        user.observeForever {
            Log.d(Constants.LogTag, "Changed user: $it")
        }
    }

    fun logout() {
        Coroutines.io {
            user.value?.let { userDao.delete(it) }
        }
    }

    suspend fun login(username: String, password: String): Result<User> {
        val result = Coroutines.ioResult {
            loginServer(username, password)
        }
        if (result is Result.Success) {
            val loggedInUser = result.data
            Coroutines.io {
                userDao.upsert(loggedInUser)
            }
        } else {
            Log.d(Constants.LogTag, result.toString())
        }
        return result
    }

    private fun loginServer(username: String, password: String): Result<User> {
        val jsonObject = JSONObject(mapOf("username" to username, "password" to password))

        return when (val result =
            ServerComm.postServer(Constants.ServerURL + "auth/token/", jsonObject.toString(), this)) {
            is Result.Success -> {
                try {
                    val token = result.data.getString("token")
                    val email = result.data.getString("email")
                    val firstName = result.data.getString("first_name")
                    val lastName = result.data.getString("last_name")
                    val user = User(username, token, email, firstName, lastName)
                    Result.Success(user)
                } catch (e: Throwable) {
                    Result.Error(IOException("No token returned.", e))
                }
            }
            is Result.Error -> {
                Log.d(Constants.LogTag, "Error logging in.", result.exception)
                Result.Error(IOException("Error logging in.", result.exception))
            }
        }
    }
}
