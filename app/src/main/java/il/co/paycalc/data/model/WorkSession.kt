package il.co.paycalc.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "work_sessions")
data class WorkSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startDateTime: Date,
    val endDateTime: Date,
    var hourlyWage: Double,
    var additionalWages: Double,
    val totalSalary: Double
)
