package il.co.paycalc.data

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import il.co.paycalc.R
import il.co.paycalc.data.model.WorkSession
import il.co.paycalc.databinding.ItemLayoutBinding
import il.co.paycalc.utils.calculateTotalSalary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import kotlin.math.min
import java.util.Locale

class EventAdapter(private var work: MutableList<WorkSession>, val callBack: ItemListener) :
    RecyclerView.Adapter<EventAdapter.ItemViewHolder>() { // כאן צריך להגדיר את המחלקה כמורשה מ-RecyclerView.Adapter

    interface ItemListener {
        fun onItemClicked(index: Int)
        fun onItemLongClicked(index: Int)
    }

    inner class ItemViewHolder(private val binding: ItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener,
        View.OnLongClickListener {

        init {
            binding.root.setOnClickListener(this)
            binding.root.setOnLongClickListener(this)
        }

        override fun onClick(p0: View?) {
            callBack.onItemClicked(adapterPosition)
        }

        override fun onLongClick(p0: View?): Boolean {
            callBack.onItemLongClicked(adapterPosition)
            return true
        }

        fun bind(work: WorkSession) {
            val dateFormatDayMonthYear = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            val dateFormatDayMonth = SimpleDateFormat("dd/MM", Locale.getDefault())
            val sharedPreferences = itemView.context.getSharedPreferences("your_preference_file_key", Context.MODE_PRIVATE)
            val restStartHour = sharedPreferences.getInt("rest_start_hour_key", 16) // ערך ברירת מחדל 16

            // מיפוי ידני להסרת "יום" והמרה באמצעות מחרוזות משאבים
            val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

            val startDay = dayFormat.format(work.startDateTime)
            val endDay = dayFormat.format(work.endDateTime)

            val formattedDay = if (startDay == endDay) {
                startDay.replace("יום ", "")
            } else {
                "${startDay.replace("יום ", "")}-${endDay.replace("יום ", "")}"
            }

            binding.dayTextView.text = formattedDay

            val startDate = Calendar.getInstance().apply { time = work.startDateTime }
            val endDate = Calendar.getInstance().apply { time = work.endDateTime }

            val formattedDate = when {
                // מקרה בו השנה שונה - הצגה מלאה של התאריכים
                startDate.get(Calendar.YEAR) != endDate.get(Calendar.YEAR) -> {
                    dateFormatDayMonthYear.format(work.startDateTime) + "-" + dateFormatDayMonthYear.format(work.endDateTime)
                }
                // מקרה בו החודש שונה - הצגת היום, חודש ושנה עבור תאריך ההתחלה והסוף
                startDate.get(Calendar.MONTH) != endDate.get(Calendar.MONTH) -> {
                    dateFormatDayMonth.format(work.startDateTime) + "-" + dateFormatDayMonthYear.format(work.endDateTime)
                }
                // מקרה בו היום שונה באותו חודש - הצגת הימים, החודש והשנה עבור תאריכים באותו חודש
                startDate.get(Calendar.DAY_OF_MONTH) != endDate.get(Calendar.DAY_OF_MONTH) -> {
                    startDate.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0') +
                            "-" + endDate.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0') +
                            "/" + dateFormatDayMonthYear.format(work.startDateTime).substring(3)
                }
                // מקרה בו היום זהה - הצגת התאריך המלא
                else -> {
                    dateFormatDayMonthYear.format(work.startDateTime)
                }
            }

            binding.dateTextView.text = formattedDate

            // שעות התחלה וסיום
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val startTime = timeFormat.format(work.startDateTime)
            val endTime = timeFormat.format(work.endDateTime)
            binding.timeTextView.text = "$startTime - $endTime"

            // חישוב כמות שעות המשמרת
            val totalHours = (work.endDateTime.time - work.startDateTime.time) / (1000 * 60 * 60).toDouble()
            binding.workHoursTextView.text = String.format(Locale.getDefault(), "%.1f", totalHours)

            // שכר
            val totalSalary = calculateTotalSalary(
                itemView.context,  // שינוי מ-requireContext() ל-itemView.context
                work.startDateTime,
                work.endDateTime,
                work.hourlyWage,
                work.additionalWages,
                restStartHour
            )


            binding.salaryTextView.text = String.format(Locale.getDefault(), "%.2f₪", totalSalary)


            val totalMinutes = (work.endDateTime.time - work.startDateTime.time) / (1000 * 60)

            // מחלקים את המשמרת לשעות ומחשבים את מספר הדקות שנמצאות בכל משמרת
            val morningMinutes = calculateShiftMinutes(work.startDateTime, work.endDateTime, 7, 15)
            val afternoonMinutes = calculateShiftMinutes(work.startDateTime, work.endDateTime, 15, 23)
            val nightMinutes = totalMinutes - morningMinutes - afternoonMinutes

            // בוחרים את המשמרת בעלת הרוב הדקות ומגדירים את הצבע
            when {
                morningMinutes >= afternoonMinutes && morningMinutes >= nightMinutes -> {
                    binding.root.setBackgroundResource(R.drawable.background_morning_gradient)
                    setTextColorOnLightBackground(binding)
                }
                afternoonMinutes >= morningMinutes && afternoonMinutes >= nightMinutes -> {
                    binding.root.setBackgroundResource(R.drawable.background_afternoon_gradient)
                    setTextColorOnDarkBackground(binding)
                }
                else -> {
                    binding.root.setBackgroundResource(R.drawable.background_night_gradient)
                    setTextColorOnDarkBackground(binding)
                }
            }
        }

        private fun calculateShiftMinutes(startDateTime: Date, endDateTime: Date, shiftStartHour: Int, shiftEndHour: Int): Long {
            val calendar = Calendar.getInstance()

            // מגדירים את השעה שבה המשמרת מתחילה ומסתיימת
            val shiftStart = calendar.apply {
                time = startDateTime
                set(Calendar.HOUR_OF_DAY, shiftStartHour)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.timeInMillis

            val shiftEnd = calendar.apply {
                time = startDateTime
                set(Calendar.HOUR_OF_DAY, shiftEndHour)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.timeInMillis

            // מחשבים את הזמן האמיתי שנמצא בתוך המשמרת
            val actualStart = maxOf(shiftStart, startDateTime.time)
            val actualEnd = minOf(shiftEnd, endDateTime.time)

            // מחזירים את הזמן אם הוא נמצא במשמרת, אחרת מחזירים 0
            return if (actualStart < actualEnd) {
                (actualEnd - actualStart) / (1000 * 60)
            } else {
                0
            }
        }


        private fun setTextColorOnLightBackground(binding: ItemLayoutBinding) {
            val textColor = ContextCompat.getColor(itemView.context, R.color.text_color_on_light)
            binding.dateTextView.setTextColor(textColor)
            binding.timeTextView.setTextColor(textColor)
            binding.dayTextView.setTextColor(textColor)
            binding.salaryTextView.setTextColor(textColor)
            binding.hoursTextView.setTextColor(textColor)
            binding.workHoursTextView.setTextColor(textColor)  // הוספת צבע לכמות השעות
        }

        private fun setTextColorOnDarkBackground(binding: ItemLayoutBinding) {
            val textColor = ContextCompat.getColor(itemView.context, R.color.text_color_on_dark)
            binding.dateTextView.setTextColor(textColor)
            binding.timeTextView.setTextColor(textColor)
            binding.hoursTextView.setTextColor(textColor)
            binding.dayTextView.setTextColor(textColor)
            binding.salaryTextView.setTextColor(textColor)
            binding.workHoursTextView.setTextColor(textColor)  // הוספת צבע לכמות השעות
        }




    }

    fun itemAt(position: Int) = work[position]

    fun updateWorkSessions(newWork: List<WorkSession>) {
        work.clear()
        work.addAll(newWork)
        notifyDataSetChanged()
    }

    // פונקציה זו צריכה להיות במחלקה EventAdapter ולא בתוך ItemViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ItemViewHolder(
            ItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    // פונקציה זו צריכה להיות במחלקה EventAdapter ולא בתוך ItemViewHolder
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(work[position])
    }

    // פונקציה זו צריכה להיות במחלקה EventAdapter ולא בתוך ItemViewHolder
    override fun getItemCount() = work.size

}
