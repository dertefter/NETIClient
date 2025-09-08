package com.dertefter.neticlient.ui.schedule

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.dertefter.neticore.features.schedule.model.Week
import com.dertefter.neticlient.databinding.FragmentScheduleBinding
import com.dertefter.neticlient.ui.schedule.week.WeekFragment
import com.dertefter.neticlient.ui.search_group.SearchGroupBottomSheet
import com.google.android.material.shape.MaterialShapeDrawable
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.dertefter.neticlient.common.item_decoration.HorizontalSpaceItemDecoration
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticore.network.ResponseType
import kotlin.collections.get


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


    fun collectingGroup(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                scheduleViewModel.currentGroup.collect { currentGroup ->
                    binding.noGroup.isGone = !currentGroup.isNullOrEmpty()
                    binding.groupButton.isGone = currentGroup.isNullOrEmpty()

                    if (currentGroup != null){
                        binding.group.text = currentGroup
                        scheduleViewModel.updateScheduleForGroup(currentGroup)
                    }
                    Log.e("collectingGroup", "currentGroup: $currentGroup")

                }
            }
        }
    }

    fun collectingSchedule(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                scheduleViewModel.schedule.collect { schedule ->
                    binding.weekFragmentContainer.isGone = schedule == null
                    if (schedule != null){
                        val weekNumber = scheduleViewModel.weekNumber.first()
                        if (weekNumber != null){
                            loadWeekFragment(schedule.weeks[weekNumber - 1])
                        }
                        weeksAdapter.updateWeeks(schedule.weeks)
                    }
                }
            }
        }
    }

    fun collectingStatus(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                scheduleViewModel.status.collect { status ->
                    binding.skeleton.isVisible = status == ResponseType.LOADING &&  binding.weekFragmentContainer.isGone
                }
            }
        }
    }

    fun collectingWeekNumber(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                scheduleViewModel.weekNumber.collect { weekNumber ->
                    if (weekNumber != null){
                        weeksAdapter.updateCurrentWeekNumber(weekNumber)
                    }
                }
            }
        }
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
            binding.c1.alpha = alpha
            binding.weekGroupContainer.alpha = alpha
            binding.weeksContainer.alpha = alpha
        }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        setupAppBar()

        setupClickListeners()

        collectingSchedule()
        collectingStatus()
        collectingWeekNumber()
        collectingGroup()

        binding.weeksRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.weekButton.setOnClickListener { binding.weeksContainer.isGone = !binding.weeksContainer.isGone }
        binding.weeksRecyclerView.addItemDecoration(
            HorizontalSpaceItemDecoration(
                R.dimen.margin_min
            )
        )
        binding.weeksRecyclerView.adapter = weeksAdapter
    }

    fun setWeekText(text: String){
        binding.w.text = text
    }

    private fun loadWeekFragment(week: Week) {
        binding.weeksContainer.isGone = true
        setWeekText(week.weekNumber.toString())
        weeksAdapter.updateCurrentWeekNumber(week.weekNumber)
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
            val transition = AutoTransition().apply {
                duration = 200
            }
            TransitionManager.beginDelayedTransition(binding.weeksContainer, transition)

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
        binding.calendarButton.setOnClickListener {
            findNavController().navigate(
                R.id.calendarFragment,
                null,
                Utils.getNavOptions(),
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
