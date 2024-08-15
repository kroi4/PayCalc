package il.co.paycalc.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import il.co.paycalc.R
import il.co.paycalc.data.localDb.EventDatabase
import il.co.paycalc.data.model.WorkSession
import il.co.paycalc.data.repository.WorkSessionRepository
import il.co.paycalc.databinding.AddItemLayoutBinding
import il.co.paycalc.ui.viewmodel.WorkSessionViewModel
import il.co.paycalc.ui.viewmodel.WorkSessionViewModelFactory
import il.co.paycalc.utils.autoCleared
import il.co.paycalc.utils.calculateTotalSalary
import il.co.paycalc.utils.showToast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.min

class AddItemFragment : Fragment(R.layout.add_item_layout) {

    private var binding: AddItemLayoutBinding by autoCleared()
    private var startDate: Long? = null
    private var startTime: Long? = null
    private var endDate: Long? = null
    private var endTime: Long? = null
    private var restStartHour: Int? = null

    private val workSessionViewModel: WorkSessionViewModel by viewModels {
        WorkSessionViewModelFactory(
            requireActivity().application,
            WorkSessionRepository(
                EventDatabase.getDatabase(requireContext())!!.eventDao()
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = AddItemLayoutBinding.inflate(layoutInflater)

        // Disable the end time button initially
        binding.buttonSelectEndTime.isEnabled = false

        val calendar = Calendar.getInstance()
        startDate = calendar.timeInMillis
        endDate = calendar.timeInMillis

        // Set default dates in the required format
        binding.buttonSelectStartDate.text = SimpleDateFormat("EEE, d MMM yyyy", Locale("he")).format(calendar.time)
        binding.buttonSelectEndDate.text = SimpleDateFormat("EEE, d MMM yyyy", Locale("he")).format(calendar.time)

        binding.apply {
            radioGroupShiftType.setOnCheckedChangeListener { _, checkedId ->
                val calendar = Calendar.getInstance().apply { timeInMillis = startDate ?: 0L }
                when (checkedId) {
                    R.id.radioMorning -> {
                        startTime = workSessionViewModel.morningShiftStartTime
                        endTime = workSessionViewModel.morningShiftEndTime
                        endDate = startDate // תאריך סיום זהה לתאריך התחלה
                        binding.buttonSelectEndDate.text = SimpleDateFormat("EEE, d MMM yyyy", Locale("he")).format(endDate)
                    }
                    R.id.radioAfternoon -> {
                        startTime = workSessionViewModel.eveningShiftStartTime
                        endTime = workSessionViewModel.eveningShiftEndTime
                        endDate = startDate // תאריך סיום זהה לתאריך התחלה
                        binding.buttonSelectEndDate.text = SimpleDateFormat("EEE, d MMM yyyy", Locale("he")).format(endDate)
                    }
                    R.id.radioNight -> {
                        startTime = workSessionViewModel.nightShiftStartTime
                        endTime = workSessionViewModel.nightShiftEndTime
                        calendar.add(Calendar.DATE, 1) // הוספת יום אחד לתאריך הסיום
                        endDate = calendar.timeInMillis
                        binding.buttonSelectEndDate.text = SimpleDateFormat("EEE, d MMM yyyy", Locale("he")).format(endDate)
                    }
                }


                // Enable the end time button when any shift type is selected
                binding.buttonSelectEndTime.isEnabled = true

                updateShiftTimeButtons()
                updateCalculatedResult()
            }

            buttonSelectStartDate.setOnClickListener {
                val calendar = Calendar.getInstance().apply {
                    if (startDate != null) {
                        timeInMillis = startDate!!
                    }
                }
                DatePickerDialog(
                    requireContext(),
                    { _, year, monthOfYear, dayOfMonth ->
                        calendar.set(year, monthOfYear, dayOfMonth)
                        startDate = calendar.timeInMillis
                        binding.buttonSelectStartDate.text = SimpleDateFormat("EEE, d MMM yyyy", Locale("he")).format(calendar.time)

                        // העתקת תאריך התחלה לתאריך סיום
                        if (binding.radioGroupShiftType.checkedRadioButtonId != R.id.radioNight) {
                            endDate = startDate
                        } else {
                            // אם זה משמרת לילה, תאריך הסיום יהיה יום אחרי
                            calendar.add(Calendar.DATE, 1)
                            endDate = calendar.timeInMillis
                        }
                        binding.buttonSelectEndDate.text = SimpleDateFormat("EEE, d MMM yyyy", Locale("he")).format(endDate)

                        // הפעלת כפתור בחירת שעת סיום מאחר ותאריך התחלה נבחר
                        binding.buttonSelectEndTime.isEnabled = true

                        updateCalculatedResult()
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }

            buttonSelectStartTime.setOnClickListener {
                showTimePicker(startTime) { time ->
                    startTime = time
                    binding.buttonSelectStartTime.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(time))

                    // Enable the end time button when start time is selected
                    binding.buttonSelectEndTime.isEnabled = true

                    updateCalculatedResult()
                }
            }

            buttonSelectEndDate.setOnClickListener {
                val calendar = Calendar.getInstance().apply {
                    if (endDate != null) {
                        timeInMillis = endDate!!
                    }
                }
                DatePickerDialog(
                    requireContext(),
                    { _, year, monthOfYear, dayOfMonth ->
                        calendar.set(year, monthOfYear, dayOfMonth)
                        val selectedEndDate = calendar.timeInMillis

                        if (selectedEndDate >= startDate!!) {
                            endDate = selectedEndDate
                            binding.buttonSelectEndDate.text = SimpleDateFormat("EEEE, d MMM yyyy", Locale("he")).format(endDate)
                        } else {
                            // הצגת הודעה למשתמש אם תאריך הסיום קטן מתאריך ההתחלה
                            showToast(requireContext(), getString(R.string.end_date_before_start_date))
                        }

                        updateCalculatedResult()
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }

            buttonSelectEndTime.setOnClickListener {
                if (startTime == null || startDate == null) {
                    showToast(requireContext(), getString(R.string.please_set_start_time_first))
                } else {
                    showTimePicker(endTime) { selectedEndTime ->
                        if (endDate == startDate && selectedEndTime <= startTime!!) {
                            // אם תאריכים זהים ושעת הסיום קטנה משעת ההתחלה
                            showToast(requireContext(), getString(R.string.end_time_before_start_time))
                        } else {
                            endTime = selectedEndTime
                            buttonSelectEndTime.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(endTime!!))
                            updateCalculatedResult()
                        }
                    }
                }
            }

            // Listener for finish button
            finishBtn.setOnClickListener {
                if (startDate != null && startTime != null && endDate != null && endTime != null) {
                    val startDateTime = mergeDateAndTime(startDate!!, startTime!!)
                    val endDateTime = mergeDateAndTime(endDate!!, endTime!!)

                    val hourlyWage = workSessionViewModel.hourlyWage
                    val additionalWages = workSessionViewModel.additionalWages

                    val totalSalary = calculateTotalSalary(
                        requireContext(),
                        startDateTime,
                        endDateTime,
                        hourlyWage,
                        additionalWages,
                        restStartHour ?: 16,
                    )


                    val workSession = WorkSession(
                        startDateTime = startDateTime,
                        endDateTime = endDateTime,
                        hourlyWage = hourlyWage,
                        additionalWages = additionalWages,
                        totalSalary = totalSalary
                    )

                    workSessionViewModel.insertWorkSession(workSession)

                    findNavController().navigate(R.id.action_addItemFragment_to_allItemsFragment23)
                } else {
                    showToast(requireContext(), getString(R.string.please_fill_all_fields))
                }
            }

        }

        return binding.root
    }

    private fun mergeDateAndTime(dateMillis: Long, timeMillis: Long): Date {
        val dateCalendar = Calendar.getInstance().apply { timeInMillis = dateMillis }
        val timeCalendar = Calendar.getInstance().apply { timeInMillis = timeMillis }

        dateCalendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
        dateCalendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))

        return dateCalendar.time
    }

    private fun updateCalculatedResult() {
        if (startDate != null && startTime != null && endDate != null && endTime != null) {
            val startDateTime = mergeDateAndTime(startDate!!, startTime!!)
            val endDateTime = mergeDateAndTime(endDate!!, endTime!!)

            val hourlyWage = workSessionViewModel.hourlyWage
            val additionalWages = workSessionViewModel.additionalWages
            val restStartHour = this.restStartHour ?: 16

            val totalSalary = calculateTotalSalary(requireContext(),startDateTime, endDateTime, hourlyWage, additionalWages, restStartHour)

            val totalHours = (endDateTime.time - startDateTime.time) / (1000 * 60 * 60).toDouble()
            val overtimeHours = maxOf(0.0, totalHours - 8.0)

            val overtimeSalary = if (overtimeHours > 0) {
                val firstTwoOvertimeHours = min(overtimeHours, 2.0)
                val additionalOvertimeHours = maxOf(0.0, overtimeHours - 2.0)
                firstTwoOvertimeHours * hourlyWage * 1.25 + additionalOvertimeHours * hourlyWage * 1.5
            } else 0.0

            binding.calculatedResultTextView.text = getString(
                R.string.calculated_result,
                totalHours, overtimeHours, totalSalary, overtimeSalary
            )

            // Show the divider and TextView once the result is calculated
            binding.calculatedResultTextView.visibility = View.VISIBLE
            binding.dividerId.visibility = View.VISIBLE
        }
    }

    private fun updateShiftTimeButtons() {
        binding.buttonSelectStartTime.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(startTime ?: 0L))
        binding.buttonSelectEndTime.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(endTime ?: 0L))
    }

    private fun showTimePicker(initialTime: Long?, onTimeSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = initialTime ?: timeInMillis
        }
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                onTimeSelected(calendar.timeInMillis)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }
}
