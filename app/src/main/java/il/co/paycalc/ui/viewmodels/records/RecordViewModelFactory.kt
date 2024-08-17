package il.co.paycalc.ui.viewmodels.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import il.co.paycalc.data.repository.RecordRepository

class RecordViewModelFactory(
    private val recordRepository: RecordRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecordViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecordViewModel(recordRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
