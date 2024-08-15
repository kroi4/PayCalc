package il.co.paycalc.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import il.co.paycalc.data.localDb.RecordDao
import il.co.skystar.utils.Loading
import il.co.skystar.utils.Success
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date

suspend fun calculateTotalSalary(
    startDate: Date,
    endDate: Date,
    hourlyWage: Double,
    additionalWages: Double,
    restStartHour: Int,
    holidayDao: RecordDao  // הוסף את ה-DAO של החג כדי שנוכל לגשת לנתונים
): Double {
    var totalWage = 0.0
    var totalHours = 0
    var restTimeHoursCount = 0  // סופר את שעות המנוחה ברצף

    val calendar = Calendar.getInstance()
    calendar.time = startDate

    // הגדרת שעת סיום המנוחה על בסיס restStartHour + 36 שעות
    val restEndCalendar = Calendar.getInstance()
    restEndCalendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
    restEndCalendar.set(Calendar.HOUR_OF_DAY, restStartHour)
    restEndCalendar.add(Calendar.HOUR_OF_DAY, 36)

    val restEndHour = restEndCalendar.get(Calendar.HOUR_OF_DAY)
    val restEndDayOfWeek = restEndCalendar.get(Calendar.DAY_OF_WEEK)

    val workDurationInMillis = endDate.time - startDate.time
    val workDurationInHours = (workDurationInMillis / (1000 * 60 * 60)).toInt()

    for (i in 0 until workDurationInHours) {
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val currentDate = calendar.time.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

        var currentRate = 1.0

        val isNightHour = currentHour >= 22 || currentHour < 6

        // בדיקה אם מדובר ביום מנוחה (שישי/שבת) או ביום חג
        val isRestTime = (currentDayOfWeek == Calendar.SATURDAY ||
                (currentDayOfWeek == Calendar.FRIDAY && currentHour >= restStartHour)) ||
                (currentDayOfWeek == restEndDayOfWeek && currentHour < restEndHour) ||
                isHoliday(currentDate, holidayDao) // הוספת בדיקת חג

        if (isRestTime) {
            restTimeHoursCount++
            currentRate += 0.5  // תוספת מנוחה רגילה
            if (restTimeHoursCount >= 3) {
                currentRate += 0.5  // תוספת לשעה השלישית ואילך בזמן מנוחה
            }
        } else {
            restTimeHoursCount = 0  // איפוס המונה אם זה לא זמן מנוחה
        }

        if (isNightHour) {
            currentRate += 0.25
        }

        // הגבלת תעריף ל-200% (2.0)
        if (currentRate > 2.0) {
            currentRate = 2.0
        }

        val currentWage = hourlyWage * currentRate
        totalWage += currentWage
        totalHours++

        calendar.add(Calendar.HOUR_OF_DAY, 1)
    }

    totalWage += totalHours * additionalWages

    return totalWage
}

// פונקציה שבודקת האם מדובר ביום חג או ערב חג
suspend fun isHoliday(date: LocalDate, recordDao: RecordDao): Boolean {
    val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val dateAsString = date.format(dateFormat)
    val record = recordDao.getHolidayByDate(dateAsString)

    return if (record != null && record.Name.isNotEmpty()) {
        val isEve = isEveOfHoliday(record.Name)
        val isFullHoliday = isFullHoliday(record.Name)

        // אם זה ערב חג או חג שבתון - החישוב ייקח זאת בחשבון
        isEve || isFullHoliday
    } else {
        false
    }
}


private fun isEveOfHoliday(holidayName: String): Boolean {
    return listOf("ראש השנה", "יום כיפור", "פסח", "שבועות", "סוכות").contains(holidayName)
}

private fun isFullHoliday(holidayName: String): Boolean {
    return listOf("ראש השנה", "יום כיפור", "פסח", "שבועות", "סוכות", "יום העצמאות").contains(holidayName)
}


fun showToast(context: Context, text: String) {
    val toast = Toast.makeText(context, text, Toast.LENGTH_SHORT)
    toast.show()
}

