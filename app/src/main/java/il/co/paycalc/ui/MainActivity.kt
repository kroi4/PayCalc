package il.co.paycalc.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import il.co.paycalc.R
import il.co.paycalc.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // קריאה ל-API ב-thread אחורי (Dispatchers.IO)
        CoroutineScope(Dispatchers.IO).launch {
            val success = HolidayUtils.updateHolidays(this@MainActivity)
            if (success) {
                // כאן תוכל לעדכן את ה-UI אם צריך, על ידי שימוש ב-Dispatchers.Main
                CoroutineScope(Dispatchers.Main).launch {
                    // עדכון UI
                }
            }
        }
    }
}
