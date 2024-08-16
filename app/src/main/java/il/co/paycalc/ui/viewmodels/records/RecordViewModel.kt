package il.co.paycalc.ui.viewmodels.records

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import il.co.paycalc.data.model.holiday.Holiday
import il.co.paycalc.data.repository.RecordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class RecordViewModel(private val recordRepository: RecordRepository): ViewModel() {

    var departures = recordRepository.getDepartures()

    fun getRecords(){
        viewModelScope.launch(Dispatchers.IO){
            recordRepository.getRecords()
        }
    }


//    fun checkIfHoliday(date: Date): LiveData<Boolean> = liveData {
//        emit(recordRepository.isHoliday(date))
//    }

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