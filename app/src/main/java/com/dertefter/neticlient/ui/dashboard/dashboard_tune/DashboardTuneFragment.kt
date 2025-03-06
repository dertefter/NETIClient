package com.dertefter.neticlient.ui.dashboard.dashboard_tune

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.data.model.dashboard.DashboardItem
import com.dertefter.neticlient.databinding.FragmentDashboardTuneBinding
import com.dertefter.neticlient.ui.dashboard.DashboardViewModel
import com.dertefter.neticlient.utils.Utils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardTuneFragment : Fragment() {

    lateinit var binding: FragmentDashboardTuneBinding
    private val dashboardViewModel: DashboardViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardTuneBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun removeItem(dashboardItem: DashboardItem) {
        dashboardViewModel.removeItem(dashboardItem)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dashboardViewModel.fetchDashboardItemList()

        val adapter = DashboardTuneRecyclerViewAdapter(this)

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())


        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                adapter.moveItem(fromPosition, toPosition)
                dashboardViewModel.moveItem(fromPosition, toPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }

            override fun isLongPressDragEnabled(): Boolean {
                return true
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)


        dashboardViewModel.dashboardItemListLiveData.observe(viewLifecycleOwner){
            adapter.setItems(it)
            if (it.isEmpty()){
                Utils.basicAnimationOn(binding.noItems).start()
                binding.fab.extend()
            } else {
                Utils.basicAnimationOff(binding.noItems).start()
                binding.fab.shrink()
            }
        }


    }

}