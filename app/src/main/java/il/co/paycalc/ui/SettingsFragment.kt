package il.co.paycalc.ui.screens

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

class SettingsFragment : Fragment(R.layout.settings_layout) {

    private var binding: SettingsLayoutBinding by autoCleared()

    private val workSessionViewModel: WorkSessionViewModel by viewModels {
        WorkSessionViewModelFactory(
            requireActivity().application, // העברת Application
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

            saveButton.setOnClickListener {
                val hourlyWage = binding.hourlyWageEditText.text.toString().toDoubleOrNull() ?: 0.0
                val additionalWages = binding.additionalWagesEditText.text.toString().toDoubleOrNull() ?: 0.0

                // עדכון שכר בסיס ותוספת ב-ViewModel
                workSessionViewModel.updateWages(hourlyWage, additionalWages)

                // נווט חזרה למסך הקודם
                findNavController().navigate(R.id.action_settingsFragment_to_allItemsFragment2)
            }
        }

        return binding.root
    }
}
