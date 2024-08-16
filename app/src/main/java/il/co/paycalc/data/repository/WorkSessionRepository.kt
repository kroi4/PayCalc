package il.co.paycalc.data.repository

import androidx.lifecycle.LiveData
import il.co.paycalc.data.localDb.events.EventDao
import il.co.paycalc.data.model.WorkSession

class WorkSessionRepository(private val eventDao: EventDao) {

    suspend fun insertWorkSession(workSession: WorkSession) {
        eventDao.insertWorkSession(workSession)
    }

    suspend fun getAllWorkSessions(): List<WorkSession> {
        return eventDao.getAllWorkSessions()
    }

    fun getWorkSessionById(workSessionId: Int): LiveData<WorkSession?> {
        return eventDao.getWorkSessionById(workSessionId)
    }

    suspend fun deleteWorkSession(workSession: WorkSession) {
        eventDao.deleteWorkSession(workSession)
    }

    suspend fun updateWages(hourlyWage: Double, additionalWages: Double) {
        val allSessions = eventDao.getAllWorkSessions()
        allSessions.forEach { session ->
            session.hourlyWage = hourlyWage
            session.additionalWages = additionalWages
            eventDao.updateWorkSession(session)
        }
    }

}
