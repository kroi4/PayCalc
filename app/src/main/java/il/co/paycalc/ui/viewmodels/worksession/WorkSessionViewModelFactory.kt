package il.co.paycalc.ui.viewmodels.worksession

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import il.co.paycalc.data.repository.WorkSessionRepository

class WorkSessionViewModelFactory(
    private val application: Application,
    private val repository: WorkSessionRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkSessionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorkSessionViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
