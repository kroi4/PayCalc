package il.co.paycalc.ui.viewmodels.records

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import il.co.paycalc.data.model.holiday.Holiday
import il.co.paycalc.data.repository.RecordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecordViewModel(private val recordRepository: RecordRepository): ViewModel() {

    var holidays = recordRepository.getHolidays()

    fun getRecords(){
        viewModelScope.launch(Dispatchers.IO){
            recordRepository.getRecords()
        }
    }


//    fun checkIfHoliday(date: Date): LiveData<Boolean> = liveData {
//        emit(recordRepository.isHoliday(date))
//    }

    fun getHolidays() {
        viewModelScope.launch(Dispatchers.IO) {
            holidays = recordRepository.getHolidays()
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