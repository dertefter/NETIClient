package com.dertefter.neticlient.ui.search_group

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.databinding.SearchGroupBottomSheetConentBinding
import com.dertefter.neticlient.ui.schedule.ScheduleViewModel
import com.dertefter.neticlient.common.item_decoration.GridSpacingItemDecoration
import com.dertefter.neticlient.common.utils.Utils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SearchGroupBottomSheet : BottomSheetDialogFragment() {

    lateinit var binding: SearchGroupBottomSheetConentBinding
    private val searchGroupViewModel: SearchGroupViewModel by activityViewModels()
    private val scheduleViewModel: ScheduleViewModel by activityViewModels()
    lateinit var _dialog: BottomSheetDialog
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SearchGroupBottomSheetConentBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        const val TAG = "SearchGroupBottomSheet"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        _dialog = dialog
        return dialog
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = GroupListRecyclerViewAdapter(emptyList(), this)

        val adapterHistory = GroupListHistoryRecyclerViewAdapter(emptyList(), this)

        val spanCount = resources.getInteger(R.integer.span_count) * 2

        binding.groupsRecyclerView.adapter = adapter
        binding.groupsRecyclerView.layoutManager = GridLayoutManager(requireContext(), spanCount)

        ViewCompat.setNestedScrollingEnabled(binding.groupsHistoryRecyclerView, false)

        binding.groupsRecyclerView.addItemDecoration(
            GridSpacingItemDecoration(
                requireContext(),
                spanCount,
                R.dimen.margin
            )
        )

        binding.groupsHistoryRecyclerView.adapter = adapterHistory
        binding.groupsHistoryRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        searchGroupViewModel.fetchGroupList("")
        searchGroupViewModel.fetchGroupList("")


        searchGroupViewModel.groupListLiveData.observe(viewLifecycleOwner){
            if (it.responseType == ResponseType.SUCCESS){
                val groupList = it.data as List<String>
                adapter.setData(groupList)
                if (groupList.isEmpty()){
                    Utils.basicAnimationOff(binding.groupsRecyclerView).start()
                    Utils.basicAnimationOn(binding.info).start()
                    binding.info.text = "Ничего не нашлось"
                }else{
                    Utils.basicAnimationOn(binding.groupsRecyclerView).start()
                    binding.info.visibility = View.GONE
                }

            } else if (it.responseType == ResponseType.ERROR)  {
                Utils.basicAnimationOff(binding.groupsRecyclerView).start()
                Utils.basicAnimationOn(binding.info).start()
                binding.info.text = "Не удалось загрузить данные"
            }
        }

        searchGroupViewModel.groupHistoryLiveData.observe(viewLifecycleOwner){
            adapterHistory.setData(it)

            for (i in 0..<binding.groupsHistoryRecyclerView.itemDecorationCount){
                binding.groupsHistoryRecyclerView.removeItemDecorationAt(i)
            }

            binding.groupsHistoryRecyclerView.addItemDecoration(
                GridSpacingItemDecoration(
                    requireContext(),
                    it.size,
                    R.dimen.margin
                )
            )


        }

        searchGroupViewModel.getGroupsHistory()


        binding.searchGroupField.editText?.doOnTextChanged { text, start, before, count ->
            searchGroupViewModel.fetchGroupList(text.toString())
        }

        binding.searchGroupField.editText?.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus){
                _dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

    }

    fun selectGroup(group: String) {
        scheduleViewModel.setSelectedGroup(group)
        dismiss()
    }

    fun remove(item: String) {
        searchGroupViewModel.removeGroupFromHistory(item)
    }


}