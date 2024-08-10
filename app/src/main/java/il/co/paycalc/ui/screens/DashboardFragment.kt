package il.co.paycalc.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import il.co.paycalc.R
import il.co.paycalc.databinding.DashboardLayoutBinding
import il.co.paycalc.utils.autoCleared

class DashboardFragment : Fragment(R.layout.dashboard_layout) {

    private var binding: DashboardLayoutBinding by autoCleared()
//    private val viewModel: EventViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DashboardLayoutBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        ItemTouchHelper(object : ItemTouchHelper.Callback() {
//            override fun getMovementFlags(
//                recyclerView: RecyclerView,
//                viewHolder: RecyclerView.ViewHolder
//            ) = makeFlag(
//                ItemTouchHelper.ACTION_STATE_SWIPE,
//                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
//            )
//
//            override fun onMove(
//                recyclerView: RecyclerView,
//                viewHolder: RecyclerView.ViewHolder,
//                target: RecyclerView.ViewHolder
//            ): Boolean {
//                TODO("Not yet implemented")
//            }
//
//            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
//        }).attachToRecyclerView(binding.tilesRecyclerView)
    }

//    fun setRecyclerViewer(it: Resource<List<Record>>): RecordsAdapter {
//        return RecordsAdapter(it.status.data!!, object : RecordsAdapter.ItemListener {
//
//            override fun onItemClicked(index: Int) {
//                itemBinding = DetailsFlightLayoutBinding.inflate(layoutInflater)
//
//                val builder = AlertDialog.Builder(requireContext())
//                builder.setView(itemBinding.root)
//                val dialog = builder.create()
//                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//
//                itemBinding.apply {
//                }
//            }
//
//            override fun onItemLongClicked(index: Int) {
//                //                    viewModel.setItem(it.result.records!![index])
//                //                    Toast.makeText(
//                //                        requireContext(),
//                //                        "${it.result.records!![index]}", Toast.LENGTH_SHORT
//                //                    ).show()
//            }
//        })
//    }


}
