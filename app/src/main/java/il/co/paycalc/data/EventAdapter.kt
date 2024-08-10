package il.co.paycalc.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import il.co.paycalc.data.model.WorkSession
import il.co.paycalc.databinding.ItemLayoutBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import kotlin.math.min
import java.util.Locale

class EventAdapter(private var work: MutableList<WorkSession>, val callBack: ItemListener) :
    RecyclerView.Adapter<EventAdapter.ItemViewHolder>() {

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
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            val formattedStartDate = dateFormat.format(work.startDateTime)
            val formattedEndDate = dateFormat.format(work.endDateTime)

            binding.startDateTextView.text = formattedStartDate
            binding.endDateTextView.text = formattedEndDate

            // חישוב השכר הכולל תוך התחשבות בשעות נוספות ומשמרות לילה
            val totalSalary = calculateTotalSalary(
                work.startDateTime,
                work.endDateTime,
                work.hourlyWage,
                work.additionalWages
            )

            binding.salaryTextView.text = String.format(Locale.getDefault(), "%.2f₪", totalSalary)
        }
    }

    fun itemAt(position: Int) = work[position]

    fun updateWorkSessions(newWork: List<WorkSession>) {
        work.clear()
        work.addAll(newWork)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ItemViewHolder(
            ItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(work[position])
    }

    override fun getItemCount() = work.size

    // פונקציה לחישוב השכר הכולל תוך התחשבות בשעות נוספות ומשמרות לילה
    private fun calculateTotalSalary(
        startDate: Date,
        endDate: Date,
        hourlyWage: Double,
        additionalWages: Double
    ): Double {
        val calendar = Calendar.getInstance()
        calendar.time = startDate

        // חישוב של סך השעות שהמשמרת נמשכה
        val totalHours = (endDate.time - startDate.time) / (1000 * 60 * 60).toDouble()
        var totalSalary = 0.0

        // חישוב שכר עבור משמרת לילה
        if (calendar.get(Calendar.HOUR_OF_DAY) >= 22 || calendar.get(Calendar.HOUR_OF_DAY) < 6) {
            val nightHours = totalHours
            totalSalary += nightHours * hourlyWage * 1.25
        } else {
            // חישוב השכר עבור השעות הראשונות עד 8 שעות ביום עבודה רגיל
            val normalHours = min(totalHours, 8.0)
            totalSalary += normalHours * hourlyWage

            // חישוב שעות נוספות
            if (totalHours > 8) {
                val overtimeHours = totalHours - 8
                val firstTwoOvertimeHours = min(overtimeHours, 2.0)
                val additionalOvertimeHours = maxOf(0.0, overtimeHours - 2)

                totalSalary += firstTwoOvertimeHours * hourlyWage * 1.25
                totalSalary += additionalOvertimeHours * hourlyWage * 1.5
            }
        }

        // הוספת תוספות השכר לאחר חישוב השכר הנוסף
        totalSalary += totalHours * additionalWages

        return totalSalary
    }
}
