package ch.ethz.ikg.rideshare.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*

class AppStateViewModel : ViewModel() {
    val currentDate: MutableLiveData<Calendar> by lazy {
        MutableLiveData<Calendar>()
    }
}