package il.co.paycalc.data

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class Holiday(val name: String, val start: LocalDate, val end: LocalDate)

object NetworkUtils {

    private const val TAG = "HolidayDebug"
    private const val API_URL = "https://data.gov.il/api/3/action/datastore_search?resource_id=67492cda-b36e-45f4-9ed1-0471af297e8b&limit=100"

    fun fetchHolidays(): List<Holiday> {
        val client = OkHttpClient()
        val request = Request.Builder().url(API_URL).build()
        val response = client.newCall(request).execute()
        val responseBody = response.body()?.string() ?: throw Exception("Failed to fetch data")

        val holidaysList = mutableListOf<Holiday>()
        val jsonResponse = JSONObject(responseBody)
        val records = jsonResponse.getJSONObject("result").getJSONArray("records")
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

        for (i in 0 until records.length()) {
            val record = records.getJSONObject(i)
            val name = record.getString("Name")
            val startDate = LocalDate.parse(record.getString("HolidayStart"), formatter)
            val endDate = LocalDate.parse(record.getString("HolidayEnds"), formatter)
            holidaysList.add(Holiday(name, startDate, endDate))
            holidaysList.forEach { holiday ->
                Log.d(TAG, "Holiday: ${holiday.name}, Start: ${holiday.start}, End: ${holiday.end}")
            }
        }

        // הצגת פרטי החגים האמיתיים ב-Logcat בצורה שמדמה DataFrame
        holidaysList.forEach { holiday ->
            Log.d(TAG, "Holiday: ${holiday.name}, Start: ${holiday.start}, End: ${holiday.end}")
        }

        return holidaysList
    }
}
