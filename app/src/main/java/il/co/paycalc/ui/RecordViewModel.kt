package il.co.paycalc.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import il.co.paycalc.data.model.holiday.Holiday
import il.co.paycalc.data.repository.RecordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecordViewModel(private val recordRepository: RecordRepository): ViewModel() {

    var departures = recordRepository.getDepartures()

    fun getRecords(){
        viewModelScope.launch(Dispatchers.IO){
            recordRepository.getRecords()
        }
    }

    fun getDepartures() {
        viewModelScope.launch(Dispatchers.IO) {
            departures = recordRepository.getDepartures()
        }
    }

    val records: LiveData<Holiday>
        get() = recordRepository.records


    fun clearRecords() {
        viewModelScope.launch(Dispatchers.IO){
            recordRepository.clearRecords()
        }
    }

}