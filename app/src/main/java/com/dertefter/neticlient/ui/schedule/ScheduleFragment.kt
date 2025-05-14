package com.dertefter.neticlient.ui.schedule

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.item_decoration.GridSpacingItemDecoration
import com.dertefter.neticlient.data.model.CurrentTimeObject
import com.dertefter.neticlient.data.model.schedule.Week
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.databinding.FragmentScheduleBinding
import com.dertefter.neticlient.ui.schedule.week.WeekFragment
import com.dertefter.neticlient.ui.search_group.SearchGroupBottomSheet
import com.dertefter.neticlient.widgets.schedule_widget.ScheduleWidget
import com.google.android.material.shape.MaterialShapeDrawable
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.google.android.material.color.MaterialColors


@AndroidEntryPoint
class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    val binding get() = _binding!!

    private val scheduleViewModel: ScheduleViewModel by activityViewModels()

    val weeksAdapter = WeeksAdapter(emptyList()) { selectedWeek ->
        loadWeekFragment(selectedWeek)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.appBarLayout) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = insets.top)
            WindowInsetsCompat.CONSUMED
        }

        binding.appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val totalScrollRange = appBarLayout.totalScrollRange
            val alpha = 1f - (-verticalOffset.toFloat() / totalScrollRange)
            binding.yearAndMounth.alpha = alpha
            binding.weekGroupContainer.alpha = alpha
            binding.weeksContainer.alpha = alpha
        }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        setupAppBar()

        setupClickListeners()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                scheduleViewModel.scheduleState.collect { state ->
                    when (state.responseType) {
                        ResponseType.LOADING -> showLoading()
                        ResponseType.SUCCESS -> showSchedule(state)
                        ResponseType.ERROR -> showError()
                    }
                }
            }
        }

    }

    private suspend fun showSchedule(state: ScheduleUiState) {
        binding.weekFragmentContainer.visibility = View.VISIBLE
        binding.skeleton.visibility = View.GONE
        state.group?.let { group ->
            binding.group.text = group
            state.schedule?.weeks?.let { weeks ->
                setupWeekSelector(
                    weeks = weeks
                )
            }
        }

        state.schedule?.let { schedule ->
            val currentWeekNumber = scheduleViewModel.weekNumberFlow.first()
            if (currentWeekNumber != null){
                weeksAdapter.updateCurrentWeekNumber(currentWeekNumber)
                val dayNumber = CurrentTimeObject.currentDayLiveData.value
                if (dayNumber == 7 && schedule.getWeek(currentWeekNumber + 1) != null){
                    schedule.getWeek(currentWeekNumber + 1)?.let {
                        loadWeekFragment(
                            it
                        )
                    }
                } else {
                    schedule.getWeek(currentWeekNumber)?.let {
                        loadWeekFragment(
                            it
                        )
                    }
                }

            }

        }
    }

    private fun showLoading(){
        binding.weekFragmentContainer.visibility = View.GONE
        binding.skeleton.visibility = View.VISIBLE
    }

    private fun showError(){
        binding.weekFragmentContainer.visibility = View.GONE
        binding.skeleton.visibility = View.GONE
        
    }


    fun setWeekText(text: String){
        if (text.length == 2){
            binding.w1.text = text[0].toString()
            binding.w2.text = text[1].toString()
            binding.w1.isGone = false
        } else {
            binding.w2.text = text
            binding.w1.isGone = true
        }
    }

    private fun loadWeekFragment(week: Week) {
        binding.weeksContainer.isGone = true
        setWeekText(week.weekNumber.toString())

        val bundle = Bundle().apply {
            putParcelable("WEEK", week)
        }

        val fragment = WeekFragment().apply {
            arguments = bundle
        }

        childFragmentManager.commit {
            replace(R.id.week_fragment_container, fragment)
            setReorderingAllowed(true)
        }
    }


    private fun setupAppBar() {

        binding.appBarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(context)
        binding.appBarLayout.setLiftable(true)
        binding.appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val totalScrollRange = appBarLayout.totalScrollRange
            val percent = 1f - (-verticalOffset.toFloat() / totalScrollRange)
            val dayTabsPaddingH = resources.getDimension(R.dimen.margin)

            val newPaddingHorizontal = dayTabsPaddingH - (dayTabsPaddingH - dayTabsPaddingH * percent)

            val newPaddingVertical = dayTabsPaddingH - (dayTabsPaddingH - dayTabsPaddingH * percent) * 0.5

            binding.daysTabsContainer.updatePadding(left = newPaddingHorizontal.toInt(), right = newPaddingHorizontal.toInt(), bottom = newPaddingVertical.toInt(), top = newPaddingVertical.toInt())
            if (verticalOffset < 0) {
                binding.appBarLayout.isLifted = true
            } else {
                binding.appBarLayout.isLifted = false
            }
        }
    }



    private fun setupWeekSelector(weeks: List<Week>) {
        weeksAdapter.updateWeeks(weeks)
        binding.weekButton.setOnClickListener {
            binding.weeksContainer.isGone = !binding.weeksContainer.isGone

        }
        binding.weeksRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.weeksRecyclerView.adapter = weeksAdapter
        if (binding.weeksRecyclerView.itemDecorationCount == 0){
            binding.weeksRecyclerView.addItemDecoration(
                GridSpacingItemDecoration(requireContext(), weeks.size, R.dimen.margin_min )
            )
        }

    }
    
    private fun setupClickListeners() {
        binding.searchGroupButton.setOnClickListener {
            SearchGroupBottomSheet().show(
                requireActivity().supportFragmentManager,
                SearchGroupBottomSheet.TAG
            )
        }

        binding.groupButton.setOnClickListener {
            SearchGroupBottomSheet().show(
                requireActivity().supportFragmentManager,
                SearchGroupBottomSheet.TAG
            )
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
