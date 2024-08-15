import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import il.co.paycalc.data.Holiday
import il.co.paycalc.data.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

object HolidayUtils {

    private const val HOLIDAYS_PREFS = "holidays_prefs"
    private const val HOLIDAYS_LIST_KEY = "holidays_list"

    suspend fun updateHolidays(context: Context): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val holidaysList = NetworkUtils.fetchHolidays()
                saveHolidays(context, holidaysList)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    private fun saveHolidays(context: Context, holidays: List<Holiday>) {
        val sharedPreferences = context.getSharedPreferences(HOLIDAYS_PREFS, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(holidays)
        editor.putString(HOLIDAYS_LIST_KEY, json)
        editor.apply()
    }

    fun loadHolidays(context: Context): List<Holiday> {
        val sharedPreferences = context.getSharedPreferences(HOLIDAYS_PREFS, Context.MODE_PRIVATE)
        val holidaysJson = sharedPreferences.getString(HOLIDAYS_LIST_KEY, "[]")
        return Gson().fromJson(holidaysJson, object : TypeToken<List<Holiday>>() {}.type)
    }

    fun isHoliday(context: Context, date: LocalDate): Boolean {
        val holidaysList = loadHolidays(context)
        return holidaysList.any { date.isEqual(it.start) || (date.isAfter(it.start) && date.isBefore(it.end)) || date.isEqual(it.end) }
    }
}
