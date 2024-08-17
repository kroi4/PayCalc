package il.co.paycalc.data.localDb.records

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import il.co.paycalc.data.model.holiday.Record


@Database(entities = [Record::class], version = 6, exportSchema = false)
abstract class RecordDatabase : RoomDatabase() {

    abstract fun recordDao(): RecordDao

    companion object{
        @Volatile
        private var INSTANCE: RecordDatabase? = null

        fun getDatabase(context: Context) : RecordDatabase? {
            //          if (INSTANCE != null){
            //               INSTANCE?.recordDao()?.clearRecords()
            //           }
            if (INSTANCE == null){
                synchronized(this) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        RecordDatabase::class.java,
                        "records_database"
                    ).fallbackToDestructiveMigration().build()
                }
            }

            return INSTANCE!!
        }
    }
}