package il.co.paycalc.data.localDb.records

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import il.co.paycalc.data.model.holiday.Record
import java.util.Date

@Dao
interface RecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord (record : List<Record>)

    @Query("DELETE FROM records")
    fun clearRecords()

    @Query("SELECT * FROM records")
    fun getRecords(): LiveData<List<Record>>

    @Query("SELECT * FROM records")
    suspend fun getAllHolidays(): List<Record>

    @Query("SELECT * FROM records WHERE :date BETWEEN HolidayStart AND HolidayEnds")
    suspend fun getHolidayByDate(date: String): Record?

}