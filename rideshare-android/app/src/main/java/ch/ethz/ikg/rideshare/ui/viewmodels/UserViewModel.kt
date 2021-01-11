package ch.ethz.ikg.rideshare.ui.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import ch.ethz.ikg.rideshare.data.model.User
import ch.ethz.ikg.rideshare.data.repos.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

class UserViewModel(application: Application) : ViewModel(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + SupervisorJob()

    private var userRepository: UserRepository = UserRepository(application)

    fun getUser(): LiveData<User> = userRepository.user
}
