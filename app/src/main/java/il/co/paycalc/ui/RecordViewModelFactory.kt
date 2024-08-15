package il.co.paycalc.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import il.co.paycalc.data.repository.RecordRepository

class RecordViewModelFactory(private val recordRepository: RecordRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RecordViewModel(recordRepository) as T
    }
}