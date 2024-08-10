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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.min

class AddItemFragment : Fragment(R.layout.add_item_layout) {

    private var binding: AddItemLayoutBinding by autoCleared()
    private var startDate: Long? = null
    private var endDate: Long? = null

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

        binding.apply {

            buttonSelectStartDateTime.setOnClickListener {
                val calendar = Calendar.getInstance()
                DatePickerDialog(
                    requireContext(),
                    { _, year, monthOfYear, dayOfMonth ->
                        TimePickerDialog(
                            requireContext(),
                            { _, hourOfDay, minute ->
                                calendar.set(year, monthOfYear, dayOfMonth, hourOfDay, minute)
                                startDate = calendar.timeInMillis

                                val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(calendar.time)
                                buttonSelectStartDateTime.text = formattedDate

                                if (startDate != null && endDate != null) {
                                    val hourlyWage = workSessionViewModel.hourlyWage
                                    val additionalWages = workSessionViewModel.additionalWages
                                    val totalSalary = calculateTotalSalary(
                                        Date(startDate!!),
                                        Date(endDate!!),
                                        hourlyWage,
                                        additionalWages
                                    )
                                    binding.calculatedResultTextView.text = String.format(Locale.getDefault(), "%.2f₪", totalSalary)
                                }
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        ).show()
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }

            buttonSelectEndDateTime.setOnClickListener {
                val calendar = Calendar.getInstance()
                DatePickerDialog(
                    requireContext(),
                    { _, year, monthOfYear, dayOfMonth ->
                        TimePickerDialog(
                            requireContext(),
                            { _, hourOfDay, minute ->
                                calendar.set(year, monthOfYear, dayOfMonth, hourOfDay, minute)
                                endDate = calendar.timeInMillis

                                val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(calendar.time)
                                buttonSelectEndDateTime.text = formattedDate

                                if (startDate != null && endDate != null) {
                                    val hourlyWage = workSessionViewModel.hourlyWage
                                    val additionalWages = workSessionViewModel.additionalWages
                                    val totalSalary = calculateTotalSalary(
                                        Date(startDate!!),
                                        Date(endDate!!),
                                        hourlyWage,
                                        additionalWages
                                    )
                                    binding.calculatedResultTextView.text = String.format(Locale.getDefault(), "%.2f₪", totalSalary)
                                }
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        ).show()
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }

            finishBtn.setOnClickListener {
                if (startDate != null && endDate != null) {
                    val hourlyWage = workSessionViewModel.hourlyWage
                    val additionalWages = workSessionViewModel.additionalWages

                    val totalSalary = calculateTotalSalary(
                        Date(startDate!!),
                        Date(endDate!!),
                        hourlyWage,
                        additionalWages
                    )

                    val workSession = WorkSession(
                        startDateTime = Date(startDate!!),
                        endDateTime = Date(endDate!!),
                        hourlyWage = hourlyWage,
                        additionalWages = additionalWages,
                        totalSalary = totalSalary
                    )

                    workSessionViewModel.insertWorkSession(workSession)

                    findNavController().navigate(R.id.action_addItemFragment_to_allItemsFragment23)
                }
            }
        }

        return binding.root
    }

    private fun calculateTotalSalary(
        startDate: Date,
        endDate: Date,
        hourlyWage: Double,
        additionalWages: Double
    ): Double {
        val calendar = Calendar.getInstance()
        calendar.time = startDate

        val totalHours = (endDate.time - startDate.time) / (1000 * 60 * 60).toDouble()
        var totalSalary = 0.0

        if (calendar.get(Calendar.HOUR_OF_DAY) >= 22 || calendar.get(Calendar.HOUR_OF_DAY) < 6) {
            val nightHours = totalHours
            totalSalary += nightHours * hourlyWage * 1.25
        } else {
            val normalHours = min(totalHours, 8.0)
            totalSalary += normalHours * hourlyWage

            if (totalHours > 8) {
                val overtimeHours = totalHours - 8
                val firstTwoOvertimeHours = min(overtimeHours, 2.0)
                val additionalOvertimeHours = maxOf(0.0, overtimeHours - 2)

                totalSalary += firstTwoOvertimeHours * hourlyWage * 1.25
                totalSalary += additionalOvertimeHours * hourlyWage * 1.5
            }
        }

        totalSalary += totalHours * additionalWages

        return totalSalary
    }
}
