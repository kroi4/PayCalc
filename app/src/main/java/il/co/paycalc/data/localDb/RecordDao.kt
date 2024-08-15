package il.co.paycalc.data.localDb

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import il.co.paycalc.data.model.holiday.Record

@Dao
interface RecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord (record : List<Record>)

    @Query("DELETE FROM records")
    fun clearRecords()

    @Query("SELECT * FROM records")
    fun getRecords(): LiveData<List<Record>>
}