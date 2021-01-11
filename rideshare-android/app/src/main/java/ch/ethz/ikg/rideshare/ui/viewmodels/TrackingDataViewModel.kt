package ch.ethz.ikg.rideshare.ui.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import ch.ethz.ikg.rideshare.data.model.Staypoint
import ch.ethz.ikg.rideshare.data.model.Trackpoint
import ch.ethz.ikg.rideshare.data.model.Tripleg
import ch.ethz.ikg.rideshare.data.repos.TrackingDataRepository
import ch.ethz.ikg.rideshare.data.repos.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.util.*
import kotlin.coroutines.CoroutineContext

class TrackingDataViewModel(application: Application) : ViewModel(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + SupervisorJob()

    private var userRepository: UserRepository = UserRepository(application)
    private var repository: TrackingDataRepository = TrackingDataRepository(application, userRepository)

    fun getAllTrackpoints(): LiveData<List<Trackpoint>> {
        return repository.getAll()
    }

    fun getAllTrackpointsOnDate(date: Date): List<Trackpoint> {
        return repository.getAllOnDate(date)
    }

    fun deleteAllTrackpoints() {
        repository.deleteAll()
    }

    fun getAllNonSyncedTrackpoints(): LiveData<List<Trackpoint>> {
        return repository.getNonSynchronized()
    }

    fun getAllTriplegs(): LiveData<List<Tripleg>> {
        return repository.getAllTriplegs()
    }

    fun getAllTriplegsOnDate(date: Date): List<Tripleg> {
        return repository.getAllTriplegsOnDate(date)
    }

    fun getAllStaypoints(): LiveData<List<Staypoint>> {
        return repository.getAllStaypoints()
    }

    fun getAllStaypointsOnDate(date: Date): List<Staypoint> {
        return repository.getAllStaypointsOnDate(date)
    }
}
