package il.co.paycalc.utils

import android.content.Context
import il.co.paycalc.R

class PreferencesManager(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun saveLastFetchTime(time: String) {
        sharedPreferences.edit().putString("last_fetch_time", time).apply()
    }

    fun getLastFetchTime(): String? {
        return sharedPreferences.getString("last_fetch_time", context.getString(R.string.update_now))
    }
}
