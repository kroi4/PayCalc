package il.co.paycalc.ui.viewmodels.worksession

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

    // משתנים עבור שעות המשמרות
    var morningShiftStartTime: Long
        get() = sharedPreferences.getLong("morningShiftStartTime", 0L)
        private set(value) {
            sharedPreferences.edit().putLong("morningShiftStartTime", value).apply()
        }

    var morningShiftEndTime: Long
        get() = sharedPreferences.getLong("morningShiftEndTime", 0L)
        private set(value) {
            sharedPreferences.edit().putLong("morningShiftEndTime", value).apply()
        }

    var eveningShiftStartTime: Long
        get() = sharedPreferences.getLong("eveningShiftStartTime", 0L)
        private set(value) {
            sharedPreferences.edit().putLong("eveningShiftStartTime", value).apply()
        }

    var eveningShiftEndTime: Long
        get() = sharedPreferences.getLong("eveningShiftEndTime", 0L)
        private set(value) {
            sharedPreferences.edit().putLong("eveningShiftEndTime", value).apply()
        }

    var nightShiftStartTime: Long
        get() = sharedPreferences.getLong("nightShiftStartTime", 0L)
        private set(value) {
            sharedPreferences.edit().putLong("nightShiftStartTime", value).apply()
        }

    var nightShiftEndTime: Long
        get() = sharedPreferences.getLong("nightShiftEndTime", 0L)
        private set(value) {
            sharedPreferences.edit().putLong("nightShiftEndTime", value).apply()
        }

    private val _workSessions = MutableLiveData<List<WorkSession>>()
    val workSessions: LiveData<List<WorkSession>> get() = _workSessions

    fun fetchAllWorkSessions() {
        viewModelScope.launch {
            _workSessions.value = repository.getAllWorkSessions()
        }
    }

    fun getWorkSessionById(workSessionId: Int): LiveData<WorkSession?> {
        return repository.getWorkSessionById(workSessionId)
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

    // פונקציה לעדכון שעות המשמרות
    fun updateShiftTimes(
        morningStart: Long, morningEnd: Long,
        eveningStart: Long, eveningEnd: Long,
        nightStart: Long, nightEnd: Long
    ) {
        this.morningShiftStartTime = morningStart
        this.morningShiftEndTime = morningEnd
        this.eveningShiftStartTime = eveningStart
        this.eveningShiftEndTime = eveningEnd
        this.nightShiftStartTime = nightStart
        this.nightShiftEndTime = nightEnd
    }
}
