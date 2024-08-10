package il.co.paycalc.ui.viewmodel

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import il.co.paycalc.data.model.WorkSession
import il.co.paycalc.data.repository.WorkSessionRepository
import kotlinx.coroutines.launch

class WorkSessionViewModel(application: Application, private val repository: WorkSessionRepository) : AndroidViewModel(application) {

    private val sharedPreferences: SharedPreferences = application.getSharedPreferences("user_settings", Application.MODE_PRIVATE)

    // משתנים עבור שכר בסיס ותוספת
    var hourlyWage: Double
        get() = sharedPreferences.getFloat("hourlyWage", 50.0f).toDouble()
        private set(value) {
            sharedPreferences.edit().putFloat("hourlyWage", value.toFloat()).apply()
        }

    var additionalWages: Double
        get() = sharedPreferences.getFloat("additionalWages", 20.0f).toDouble()
        private set(value) {
            sharedPreferences.edit().putFloat("additionalWages", value.toFloat()).apply()
        }

    private val _workSessions = MutableLiveData<List<WorkSession>>()
    val workSessions: LiveData<List<WorkSession>> get() = _workSessions

    fun fetchAllWorkSessions() {
        viewModelScope.launch {
            _workSessions.value = repository.getAllWorkSessions()
        }
    }

    fun insertWorkSession(workSession: WorkSession) {
        viewModelScope.launch {
            repository.insertWorkSession(workSession)
            fetchAllWorkSessions() // רענון הרשימה
        }
    }

    fun deleteWorkSession(workSession: WorkSession) {
        viewModelScope.launch {
            repository.deleteWorkSession(workSession)
            fetchAllWorkSessions() // רענון הרשימה
        }
    }

    fun updateWages(hourlyWage: Double, additionalWages: Double) {
        this.hourlyWage = hourlyWage
        this.additionalWages = additionalWages
    }

}
