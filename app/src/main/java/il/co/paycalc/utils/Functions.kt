package il.co.paycalc.utils

import android.content.Context
import android.widget.Toast
import java.util.Calendar
import java.util.Date

fun calculateTotalSalary(
    startDate: Date,
    endDate: Date,
    hourlyWage: Double,
    additionalWages: Double,
    restStartHour: Int
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

        var currentRate = 1.0

        val isNightHour = currentHour >= 22 || currentHour < 6
        val isRestTime = (currentDayOfWeek == Calendar.SATURDAY ||
                (currentDayOfWeek == Calendar.FRIDAY && currentHour >= restStartHour)) ||
                (currentDayOfWeek == restEndDayOfWeek && currentHour < restEndHour)

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


fun clearHolidaysFromPreferences(context: Context) {
    val sharedPreferences = context.getSharedPreferences("holiday_preferences", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.remove("holidays")
    editor.apply()
}


fun showToast(context: Context, text: String) {
    val toast = Toast.makeText(context, text, Toast.LENGTH_SHORT)
    toast.show()
}