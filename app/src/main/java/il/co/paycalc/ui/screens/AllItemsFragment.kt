package il.co.paycalc.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import il.co.paycalc.R
import il.co.paycalc.data.EventAdapter
import il.co.paycalc.data.localDb.events.EventDatabase
import il.co.paycalc.data.localDb.records.RecordDao
import il.co.paycalc.data.localDb.records.RecordDatabase
import il.co.paycalc.data.repository.WorkSessionRepository
import il.co.paycalc.databinding.AllItemLayoutBinding
import il.co.paycalc.ui.viewmodels.records.RecordViewModel
import il.co.paycalc.ui.viewmodels.worksession.WorkSessionViewModel
import il.co.paycalc.ui.viewmodels.worksession.WorkSessionViewModelFactory
import il.co.paycalc.utils.autoCleared
import il.co.paycalc.utils.showToast
import il.co.paycalc.utils.Loading
import il.co.paycalc.utils.PreferencesManager
import il.co.paycalc.utils.Success
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AllItemsFragment : Fragment(R.layout.all_item_layout), EventAdapter.ItemListener {

    private var binding: AllItemLayoutBinding by autoCleared()
    private val viewModel: RecordViewModel by activityViewModels()
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var adapter: EventAdapter

    // יצירת ViewModel עם ה-Factory
    private val workSessionViewModel: WorkSessionViewModel by viewModels {
        WorkSessionViewModelFactory(
            requireActivity().application, // העברת Application
            WorkSessionRepository(
                EventDatabase.getDatabase(requireContext())!!.eventDao()
            )
        )
    }

    private lateinit var recordDao: RecordDao // הוספת משתנה ה-recordDao

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = AllItemLayoutBinding.inflate(inflater, container, false)

        preferencesManager = PreferencesManager(requireContext())


        // הגדרת ה-recordDao
        val database = RecordDatabase.getDatabase(requireContext())
        if (database != null) {
            recordDao = database.recordDao()
        }

        binding.apply {
            fab.setOnClickListener {
                findNavController().navigate(R.id.action_allItemsFragment2_to_addItemFragment2)
            }

            binding.settingsButton.setOnClickListener {
                findNavController().navigate(R.id.action_allItemsFragment2_to_settingsFragment2)
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchHolidays()

        binding.recycler.layoutManager = LinearLayoutManager(requireContext())

        // יצירת ה-Adapter עם כל הפרמטרים הנדרשים
        adapter = EventAdapter(mutableListOf(), this, workSessionViewModel, viewLifecycleOwner)
        binding.recycler.adapter = adapter

        binding.recycler.adapter = adapter

        // להאזין לנתונים מ-ViewModel ולעדכן את ה-RecyclerView
        workSessionViewModel.workSessions.observe(viewLifecycleOwner) { workSessions ->
            adapter.updateWorkSessions(workSessions)

            // חישוב סך השכר הכולל
            val totalSalary = workSessions.sumOf { it.totalSalary }
            binding.totalSalaryTextView.text = String.format(Locale.getDefault(), "%.2f₪", totalSalary)
        }

        // הגדרת ה-Swipe to Delete
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false // לא מאפשר הזזה
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val workSession = adapter.itemAt(position)
                // מחיקת הפריט דרך ה-ViewModel
                workSessionViewModel.deleteWorkSession(workSession)
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.recycler)

        // להביא את כל הסשנים מה-ViewModel
        workSessionViewModel.fetchAllWorkSessions()
    }

    override fun onResume() {
        super.onResume()
        // להביא מחדש את כל הסשנים כאשר המסך חוזר לפעולה
        workSessionViewModel.fetchAllWorkSessions()
    }

    override fun onItemClicked(index: Int) {
        // טיפול בלחיצה על פריט
    }

    override fun onItemLongClicked(index: Int) {
        // טיפול בלחיצה ארוכה על פריט
    }

    private fun fetchHolidays() {
        viewModel.getHolidays()
        viewModel.holidays.observe(viewLifecycleOwner) {
            when (it.status) {
                is Loading -> {}
                is Success -> {
                    if (!it.status.data.isNullOrEmpty()) {
                        val currentTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                        preferencesManager.saveLastFetchTime(currentTime)
                    }
                }
                is Error -> {
                    showToast(requireContext(),getString(R.string.couldnt_connect_to_server))
                }
                else -> {
                    showToast(requireContext(),getString(R.string.couldnt_connect_to_server))
                }
            }
        }
    }
}