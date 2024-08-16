package il.co.paycalc.data.localDb.events

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import il.co.paycalc.data.model.WorkSession

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkSession(workSession: WorkSession)

    @Update
    suspend fun updateWorkSession(workSession: WorkSession)

    @Update
    suspend fun update(workSession: WorkSession)

    @Delete
    suspend fun deleteWorkSession(workSession: WorkSession)

    @Query("SELECT * FROM work_sessions ORDER BY startDateTime DESC")
    suspend fun getAllWorkSessions(): List<WorkSession>

    @Query("SELECT * FROM work_sessions WHERE id = :workSessionId LIMIT 1")
    fun getWorkSessionById(workSessionId: Int): LiveData<WorkSession?>

    @Query("""
        SELECT ws.hourlyWage * 
               ((strftime('%s', ws.endDateTime) - strftime('%s', ws.startDateTime)) / 3600.0) + 
               ws.additionalWages AS totalWage 
        FROM work_sessions ws
        WHERE ws.id = :workSessionId
    """)
    suspend fun getTotalWageForSession(workSessionId: Int): Double
}

