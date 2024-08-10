package il.co.paycalc.data.repository

import il.co.paycalc.data.localDb.EventDao
import il.co.paycalc.data.model.WorkSession

class WorkSessionRepository(private val eventDao: EventDao) {

    suspend fun insertWorkSession(workSession: WorkSession) {
        eventDao.insertWorkSession(workSession)
    }

    suspend fun getAllWorkSessions(): List<WorkSession> {
        return eventDao.getAllWorkSessions()
    }

    suspend fun getWorkSessionById(workSessionId: Int): WorkSession? {
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
