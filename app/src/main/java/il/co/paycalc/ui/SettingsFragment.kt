package il.co.paycalc.ui.screens

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import il.co.paycalc.R
import il.co.paycalc.data.localDb.events.EventDatabase
import il.co.paycalc.data.repository.WorkSessionRepository
import il.co.paycalc.databinding.SettingsLayoutBinding
import il.co.paycalc.ui.viewmodels.worksession.WorkSessionViewModel
import il.co.paycalc.ui.viewmodels.worksession.WorkSessionViewModelFactory
import il.co.paycalc.utils.autoCleared
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SettingsFragment : Fragment(R.layout.settings_layout) {

    private var binding: SettingsLayoutBinding by autoCleared()
    private var morningShiftStartTime: Long? = null
    private var morningShiftEndTime: Long? = null
    private var eveningShiftStartTime: Long? = null
    private var eveningShiftEndTime: Long? = null
    private var nightShiftStartTime: Long? = null
    private var nightShiftEndTime: Long? = null
    private var restDayStartTime: Long? = null
    private var restDayEndTime: Long? = null

    private val workSessionViewModel: WorkSessionViewModel by viewModels {
        WorkSessionViewModelFactory(
            requireActivity().application,
            WorkSessionRepository(
                EventDatabase.getDatabase(requireContext())!!.eventDao()
            )
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeSettingsView()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SettingsLayoutBinding.inflate(layoutInflater)

        binding.apply {
            buttonSetMorningShift.setOnClickListener {
                showTimeRangePicker { startTime, endTime ->
                    morningShiftStartTime = startTime
                    morningShiftEndTime = endTime
                    binding.buttonSetMorningShift.text = formatTimeRange(startTime, endTime)
                }
            }

            buttonSetEveningShift.setOnClickListener {
                showTimeRangePicker { startTime, endTime ->
                    eveningShiftStartTime = startTime
                    eveningShiftEndTime = endTime
                    binding.buttonSetEveningShift.text = formatTimeRange(startTime, endTime)
                }
            }

            buttonSetNightShift.setOnClickListener {
                showTimeRangePicker { startTime, endTime ->
                    nightShiftStartTime = startTime
                    nightShiftEndTime = endTime
                    binding.buttonSetNightShift.text = formatTimeRange(startTime, endTime)
                }
            }

            // Rest Day Start Time Selection
            binding.buttonSetRestDayStart.setOnClickListener {
                showTimePicker { startTime ->
                    restDayStartTime = startTime
                    restDayEndTime = startTime + 36 * 60 * 60 * 1000 // 36 hours later
                    binding.buttonSetRestDayStart.text = formatTimeRange(restDayStartTime!!, restDayEndTime!!)
                }
            }

            saveButton.setOnClickListener {
                val hourlyWage = binding.hourlyWageEditText.text.toString().toDoubleOrNull() ?: 0.0
                val additionalWages = binding.additionalWagesEditText.text.toString().toDoubleOrNull() ?: 0.0

                // Update wages in ViewModel
                workSessionViewModel.updateWages(hourlyWage, additionalWages)

                // Ensure shift times are non-null before updating
                if (morningShiftStartTime != null && morningShiftEndTime != null &&
                    eveningShiftStartTime != null && eveningShiftEndTime != null &&
                    nightShiftStartTime != null && nightShiftEndTime != null) {

                    workSessionViewModel.updateShiftTimes(
                        morningShiftStartTime!!, morningShiftEndTime!!,
                        eveningShiftStartTime!!, eveningShiftEndTime!!,
                        nightShiftStartTime!!, nightShiftEndTime!!
                    )
                }

                // Save rest day start and end times
                if (restDayStartTime != null && restDayEndTime != null) {
                    val sharedPreferences = requireActivity().getSharedPreferences("shift_prefs", Context.MODE_PRIVATE)
                    sharedPreferences.edit().apply {
                        putLong("rest_day_start_time", restDayStartTime!!)
                        putLong("rest_day_end_time", restDayEndTime!!)
                        apply()
                    }
                }

                // Navigate back to the previous screen
                findNavController().navigate(R.id.action_settingsFragment_to_allItemsFragment2)
            }
        }

        return binding.root
    }

    private fun showTimeRangePicker(onTimeRangeSelected: (Long, Long) -> Unit) {
        val calendar = Calendar.getInstance()

        TimePickerDialog(
            requireContext(),
            { _, startHour, startMinute ->
                calendar.set(Calendar.HOUR_OF_DAY, startHour)
                calendar.set(Calendar.MINUTE, startMinute)
                val startTime = calendar.timeInMillis

                TimePickerDialog(
                    requireContext(),
                    { _, endHour, endMinute ->
                        calendar.set(Calendar.HOUR_OF_DAY, endHour)
                        calendar.set(Calendar.MINUTE, endMinute)
                        val endTime = calendar.timeInMillis

                        onTimeRangeSelected(startTime, endTime)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun showTimePicker(onTimeSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance()
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

    private fun formatTime(timeInMillis: Long): String {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        return formatter.format(timeInMillis)
    }

    private fun formatTimeRange(startTime: Long, endTime: Long): String {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        return "${formatter.format(startTime)} - ${formatter.format(endTime)}"
    }

    private fun initializeSettingsView() {
        val sharedPreferences = requireActivity().getSharedPreferences("shift_prefs", Context.MODE_PRIVATE)

        // Load saved hourly wage and additional wages
        val hourlyWage = workSessionViewModel.hourlyWage
        val additionalWages = workSessionViewModel.additionalWages

        // Display saved wages in the text fields
        binding.hourlyWageEditText.setText(hourlyWage.toString())
        binding.additionalWagesEditText.setText(additionalWages.toString())

        // Load saved shift times
        morningShiftStartTime = workSessionViewModel.morningShiftStartTime
        morningShiftEndTime = workSessionViewModel.morningShiftEndTime
        eveningShiftStartTime = workSessionViewModel.eveningShiftStartTime
        eveningShiftEndTime = workSessionViewModel.eveningShiftEndTime
        nightShiftStartTime = workSessionViewModel.nightShiftStartTime
        nightShiftEndTime = workSessionViewModel.nightShiftEndTime

        // Update button texts with saved times
        binding.buttonSetMorningShift.text = formatTimeRange(morningShiftStartTime!!, morningShiftEndTime!!)
        binding.buttonSetEveningShift.text = formatTimeRange(eveningShiftStartTime!!, eveningShiftEndTime!!)
        binding.buttonSetNightShift.text = formatTimeRange(nightShiftStartTime!!, nightShiftEndTime!!)

        // Load rest day times from SharedPreferences
        restDayStartTime = sharedPreferences.getLong("rest_day_start_time", 0L)
        restDayEndTime = sharedPreferences.getLong("rest_day_end_time", 0L)

        if (restDayStartTime != 0L && restDayEndTime != 0L) {
            binding.buttonSetRestDayStart.text = formatTime(restDayStartTime!!)
        }
    }
}
