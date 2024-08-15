package il.co.paycalc.data.model.holiday

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "records")
data class Record(
    val FullDescription: String,
    val HebrewDate: String,
    val HolidayEnds: String,
    val HolidayStart: String,
    val Name: String,
    val ShortDescription: String,
    @PrimaryKey
    val _id: Int
)