package il.co.paycalc.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import il.co.paycalc.R
import il.co.paycalc.data.localDb.records.RecordDao
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

    Log.d("calculateTotalSalary", "Work duration in hours: $workDurationInHours")

    for (i in 0 until workDurationInHours) {
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val currentDate = calendar.time.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

        var currentRate = 1.0

        val isNightHour = currentHour >= 22 || currentHour < 6

        // בדיקה אם מדובר ביום מנוחה (שישי/שבת) או ביום חג
        val isRestTime = (currentDayOfWeek == Calendar.SATURDAY ||
                (currentDayOfWeek == Calendar.FRIDAY && currentHour >= restStartHour) ||
                (currentDayOfWeek == restEndDayOfWeek && currentHour < restEndHour) ||
                (isHolidayRestTime(currentDate, currentHour, restStartHour, holidayDao)))

        Log.d(
            "calculateTotalSalary",
            "Checking hour $currentHour on $currentDate, isRestTime: $isRestTime, isNightHour: $isNightHour"
        )

        if (isRestTime) {
            restTimeHoursCount++
            currentRate += 0.5  // תוספת מנוחה רגילה
            if (restTimeHoursCount >= 3) {
                currentRate += 0.5  // תוספת לשעה השלישית ואילך בזמן מנוחה
            }
            Log.d("calculateTotalSalary", "Additional rate for rest time: $currentRate")
        } else {
            restTimeHoursCount = 0  // איפוס המונה אם זה לא זמן מנוחה
        }

        if (isNightHour) {
            currentRate += 0.25
            Log.d("calculateTotalSalary", "Additional rate for night hour: $currentRate")
        }

        // הגבלת תעריף ל-200% (2.0)
        if (currentRate > 2.0) {
            currentRate = 2.0
        }

        val currentWage = hourlyWage * currentRate
        totalWage += currentWage
        totalHours++

        Log.d(
            "calculateTotalSalary",
            "Current hour: $currentHour, Rate: $currentRate, Wage for hour: $currentWage, Accumulated wage: $totalWage"
        )

        calendar.add(Calendar.HOUR_OF_DAY, 1)
    }

    totalWage += totalHours * additionalWages

    Log.d(
        "calculateTotalSalary",
        "Total hours: $totalHours, Additional wages: $additionalWages, Final wage: $totalWage"
    )

    return totalWage
}

suspend fun isHoliday(date: LocalDate, recordDao: RecordDao): Boolean {
    val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    Log.d("isHoliday", "Checking holiday for date: $date")

    // שליפת רשומות החגים ממסד הנתונים
    val holidays = recordDao.getAllHolidays()

    // בדיקה אם התאריך נמצא באחד החגים המוגדרים
    holidays.forEach { holiday ->
        val startDate = LocalDate.parse(holiday.HolidayStart.substring(0, 10), dateFormat)
        val endDate = LocalDate.parse(holiday.HolidayEnds.substring(0, 10), dateFormat)

        if (date.isEqual(startDate) || date.isEqual(endDate) || (date.isAfter(startDate) && date.isBefore(
                endDate
            ))
        ) {
            // בדיקה אם החג הוא אחד מהחגים המלאים על פי הרשימה שסיפקת
            if (isFullHoliday(holiday.Name, date, startDate)) {
                return true
            }
        }
    }
    return false
}

private fun isFullHoliday(holidayName: String, date: LocalDate, startDate: LocalDate): Boolean {
    val isHoliday = when (holidayName) {
        "ראש השנה" -> isBetweenDays(date, startDate, 1, 3)
        "יום כיפור" -> isBetweenDays(date, startDate, 1, 2)
        "פסח" -> isPassoverHoliday(date, startDate)
        "שבועות" -> isBetweenDays(date, startDate, 1, 2)
        "סוכות" -> isSukkotHoliday(date, startDate)
        "יום העצמאות" -> isBetweenDays(date, startDate, 1, 1)
        else -> false
    }
    Log.d("isFullHoliday", "Holiday check for $holidayName: $isHoliday")
    return isHoliday
}

// פונקציה כללית לבדיקת טווח ימים
private fun isBetweenDays(
    date: LocalDate,
    startDate: LocalDate,
    startDay: Int,
    endDay: Int
): Boolean {
    val relativeDay = date.toEpochDay() - startDate.toEpochDay() + 1
    Log.d("isBetweenDays", "Relative day: $relativeDay, startDay: $startDay, endDay: $endDay")
    return relativeDay in startDay..endDay
}

// פונקציה לבדיקת ימי חג פסח
private fun isPassoverHoliday(date: LocalDate, startDate: LocalDate): Boolean {
    val isHoliday = isBetweenDays(date, startDate, 1, 2) || isBetweenDays(date, startDate, 7, 8)
    Log.d("isPassoverHoliday", "Passover holiday check: $isHoliday")
    return isHoliday
}

// פונקציה לבדיקת ימי חג סוכות
private fun isSukkotHoliday(date: LocalDate, startDate: LocalDate): Boolean {
    val isHoliday = isBetweenDays(date, startDate, 1, 2) || isBetweenDays(date, startDate, 8, 9)
    Log.d("isSukkotHoliday", "Sukkot holiday check: $isHoliday")
    return isHoliday
}

suspend fun getHolidayDates(
    holidayName: String,
    dayStart: Int,
    dayEnd: Int,
    holidayDao: RecordDao
): Pair<LocalDate, LocalDate>? {
    val holidays = holidayDao.getAllHolidays()

    val holiday = holidays.find { it.Name == holidayName } ?: return null

    val startDate = LocalDate.parse(
        holiday.HolidayStart.substring(0, 10),
        DateTimeFormatter.ofPattern("yyyy-MM-dd")
    )

    // נחשב את התאריכים המבוקשים בהתבסס על יום ההתחלה ויום הסיום
    val calculatedStartDate =
        startDate.plusDays(dayStart.toLong() - 1) // יום 1 הוא היום הראשון של החג
    val calculatedEndDate = startDate.plusDays(dayEnd.toLong() - 1)

    return Pair(calculatedStartDate, calculatedEndDate)
}


suspend fun isHolidayRestTime(
    date: LocalDate,
    hour: Int,
    restStartHour: Int,
    holidayDao: RecordDao
): Boolean {
//    holidayDao.deleteAllExcept("פסח")

    // קביעת תאריך ושעה לתחילת החג (בהתאמה ליום הנוכחי)
    val holidays = holidayDao.getAllHolidays()
    holidays.forEach { holiday ->

        val holidayDates = when (holiday.Name) { // שינוי כאן מ-holidays ל-holiday.Name
            "ראש השנה" -> getHolidayDates(holiday.Name, 1, 3, holidayDao)
            "יום כיפור" -> getHolidayDates(holiday.Name, 1, 1, holidayDao)
            "פסח" -> getRelevantPassoverDates(date, holidayDao)
            "שבועות" -> getHolidayDates(holiday.Name, 1, 1, holidayDao)
            "סוכות" -> getRelevantSukkotDates(date, holidayDao)
            "יום העצמאות" -> getHolidayDates(holiday.Name, 1, 1, holidayDao)
            else -> null // שינוי כאן מ-false ל-null כדי לשמור על סוג הנתונים
        }

        if (holidayDates != null) {
            val (startDate, endDate) = holidayDates
            // בדיקה האם התאריך הנוכחי נופל בטווח תאריכי החג
            if (date.isAfter(startDate.minusDays(1)) && date.isBefore(endDate.plusDays(1))) {
                // כעת, וודא שזהו חג מלא
                if (isFullHoliday(holiday.Name, date, startDate)) {
                    val startDateTime = startDate.atTime(restStartHour, 0)
                    val endDateTime = startDateTime.plusHours(36)

                    Log.d("isHolidayRestTime", "Start DateTime: $startDateTime")
                    Log.d("isHolidayRestTime", "End DateTime (36 hours later): $endDateTime")

                    // המרת LocalDate ל-LocalDateTime כדי לבדוק גם את השעה
                    val currentDateTime = date.atTime(hour, 0)
                    Log.d("isHolidayRestTime", "Current DateTime: $currentDateTime")

                    // בדיקה אם השעה נופלת בטווח המנוחה: 36 שעות מתחילת החג בלבד
                    if (currentDateTime.isAfter(startDateTime) && currentDateTime.isBefore(endDateTime)) {
                        Log.d("isHolidayRestTime", "Rest time is active within 36 hours from holiday start")
                        return true
                    }
                }
            }
        }
    }
    return false
}

// פונקציה ייחודית לפסח: בוחרת בין הטווח הראשון והשני בהתאמה לתאריך
suspend fun getRelevantPassoverDates(date: LocalDate, holidayDao: RecordDao): Pair<LocalDate, LocalDate>? {
    val holidayDates1 = getHolidayDates("פסח", 1, 2, holidayDao)
    val holidayDates2 = getHolidayDates("פסח", 7, 8, holidayDao)

    return when {
        holidayDates1 != null && date.isAfter(holidayDates1.first.minusDays(1)) && date.isBefore(holidayDates1.second.plusDays(1)) -> holidayDates1
        holidayDates2 != null && date.isAfter(holidayDates2.first.minusDays(1)) && date.isBefore(holidayDates2.second.plusDays(1)) -> holidayDates2
        else -> null
    }
}

// פונקציה ייחודית לסוכות: בוחרת בין הטווח הראשון והשני בהתאמה לתאריך
suspend fun getRelevantSukkotDates(date: LocalDate, holidayDao: RecordDao): Pair<LocalDate, LocalDate>? {
    val holidayDates1 = getHolidayDates("סוכות", 1, 2, holidayDao)
    val holidayDates2 = getHolidayDates("סוכות", 8, 9, holidayDao)

    return when {
        holidayDates1 != null && date.isAfter(holidayDates1.first.minusDays(1)) && date.isBefore(holidayDates1.second.plusDays(1)) -> holidayDates1
        holidayDates2 != null && date.isAfter(holidayDates2.first.minusDays(1)) && date.isBefore(holidayDates2.second.plusDays(1)) -> holidayDates2
        else -> null
    }
}


fun showToast(context: Context, text: String) {
    val toast = Toast.makeText(context, text, Toast.LENGTH_SHORT)
    toast.show()
}