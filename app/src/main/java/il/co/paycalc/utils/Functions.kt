package il.co.paycalc.utils

import android.content.Context
import android.widget.Toast
import java.util.Calendar
import java.util.Date
import kotlin.math.min

fun calculateTotalSalary(
    startDate: Date,
    endDate: Date,
    hourlyWage: Double,
    additionalWages: Double
): Double {
    val calendar = Calendar.getInstance()
    calendar.time = startDate

    var totalSalary = 0.0
    var currentHour = calendar.get(Calendar.HOUR_OF_DAY)
    var remainingHours = (endDate.time - startDate.time) / (1000 * 60 * 60).toDouble()

    while (remainingHours > 0) {
        val hoursInThisSegment = min(1.0, remainingHours)
        val isNightHour = currentHour >= 22 || currentHour < 6
        val wageMultiplier = if (isNightHour) 1.25 else 1.0

        totalSalary += hoursInThisSegment * hourlyWage * wageMultiplier
        totalSalary += hoursInThisSegment * additionalWages

        remainingHours -= hoursInThisSegment
        currentHour = (currentHour + 1) % 24
    }

    return totalSalary
}

fun showToast(context: Context, text: String) {
    val toast = Toast.makeText(context, text, Toast.LENGTH_SHORT)
    toast.show()
}