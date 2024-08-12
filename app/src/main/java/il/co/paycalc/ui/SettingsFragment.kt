package il.co.paycalc.ui.screens

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
import il.co.paycalc.data.repository.WorkSessionRepository
import il.co.paycalc.databinding.SettingsLayoutBinding
import il.co.paycalc.ui.viewmodel.WorkSessionViewModel
import il.co.paycalc.ui.viewmodel.WorkSessionViewModelFactory
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
        binding = SettingsLayoutBinding.inflate(layoutInflater)

        binding.apply {
            buttonSetMorningShift.setOnClickListener {
                showTimeRangePicker { startTime, endTime ->
                    morningShiftStartTime = startTime
                    morningShiftEndTime = endTime
                    // Update button text with selected time range
                    binding.buttonSetMorningShift.text = formatTimeRange(startTime, endTime)
                }
            }

            buttonSetEveningShift.setOnClickListener {
                showTimeRangePicker { startTime, endTime ->
                    eveningShiftStartTime = startTime
                    eveningShiftEndTime = endTime
                    // Update button text with selected time range
                    binding.buttonSetEveningShift.text = formatTimeRange(startTime, endTime)
                }
            }

            buttonSetNightShift.setOnClickListener {
                showTimeRangePicker { startTime, endTime ->
                    nightShiftStartTime = startTime
                    nightShiftEndTime = endTime
                    // Update button text with selected time range
                    binding.buttonSetNightShift.text = formatTimeRange(startTime, endTime)
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

                // Navigate back to the previous screen
                findNavController().navigate(R.id.action_settingsFragment_to_allItemsFragment2)
            }
        }

        return binding.root
    }

    private fun showTimeRangePicker(onTimeRangeSelected: (Long, Long) -> Unit) {
        val calendar = Calendar.getInstance()

        // Select start time
        TimePickerDialog(
            requireContext(),
            { _, startHour, startMinute ->
                calendar.set(Calendar.HOUR_OF_DAY, startHour)
                calendar.set(Calendar.MINUTE, startMinute)
                val startTime = calendar.timeInMillis

                // Select end time
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

    private fun formatTimeRange(startTime: Long, endTime: Long): String {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        return "${formatter.format(startTime)} - ${formatter.format(endTime)}"
    }
}
