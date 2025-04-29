package com.dertefter.neticlient.ui.schedule

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticlient.data.model.CurrentTimeObject
import com.dertefter.neticlient.data.model.schedule.Schedule
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.databinding.FragmentScheduleBinding
import com.dertefter.neticlient.ui.schedule.lesson_view.LessonDetailViewModel
import com.dertefter.neticlient.ui.schedule.lesson_view.LessonViewBottomSheetFragment
import com.dertefter.neticlient.ui.schedule.week.WeekFragment
import com.dertefter.neticlient.ui.search_group.SearchGroupBottomSheet
import com.dertefter.neticlient.ui.settings.SettingsViewModel
import com.dertefter.neticlient.widgets.schedule_widget.ScheduleWidget
import com.google.android.material.shape.MaterialShapeDrawable
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    val binding get() = _binding!!

    private val scheduleViewModel: ScheduleViewModel by activityViewModels()
    private val lessonDetailViewModel: LessonDetailViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()

    private var currentWeekNumber: Int? = null
    private var currentGroup: String? = null
    private var weekFragmentLoaded = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun loadWeekFragment(weekNumber: Int, group: String) {
        binding.weekButton.text = getString(R.string.week_menu) + " $weekNumber"

        if (currentWeekNumber == weekNumber && currentGroup == group && weekFragmentLoaded) {
            return
        }

        currentWeekNumber = weekNumber
        currentGroup = group
        weekFragmentLoaded = true
        
        val fragment = WeekFragment().apply {
            arguments = bundleOf(
                "WEEK_NUMBER" to weekNumber,
                "GROUP" to group
            )
        }
        childFragmentManager.commit {
            replace(R.id.week_fragment_container, fragment)
            setReorderingAllowed(true)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        setupAppBar()
        setupObservers()
        setupClickListeners()

        // Initialize data if needed
        if (scheduleViewModel.selectedGroupLiveData.value == null) {
            scheduleViewModel.getSelectedGroup()
        }
        if (CurrentTimeObject.currentWeekLiveData.value == null) {
            scheduleViewModel.fetchCurrentWeekNumber()
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
    
    private fun setupObservers() {
        scheduleViewModel.selectedGroupLiveData.observe(viewLifecycleOwner) { group ->
            updateWidgets()
            
            if (group.isNullOrEmpty()) {
                binding.appBarLayout.visibility = View.GONE
                binding.skeleton.visibility = View.GONE
                binding.noGroup.visibility = View.VISIBLE
            } else {
                binding.appBarLayout.visibility = View.VISIBLE
                binding.noGroup.visibility = View.GONE
                binding.groupButton.text = group

                viewLifecycleOwner.lifecycleScope.launch {
                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        scheduleViewModel.getScheduleLiveData(group).observe(viewLifecycleOwner) { response ->
                            if (response.responseType == ResponseType.SUCCESS && response.data != null) {
                                binding.skeleton.visibility = View.GONE
                                binding.weekFragmentContainer.visibility = View.VISIBLE

                                val schedule = response.data as Schedule?
                                val weeks = schedule?.weeks
                                
                                if (!weeks.isNullOrEmpty()) {
                                    val weekNumbers: List<Int> = weeks.map { it.weekNumber }
                                    setupWeekSelector(weekNumbers, group)
                                    loadCurrentWeek(weekNumbers, group)
                                }
                            } else {
                                binding.skeleton.visibility = View.VISIBLE
                                binding.weekFragmentContainer.visibility = View.GONE
                            }
                        }
                    }
                }

                if (scheduleViewModel.getScheduleLiveData(group).value?.data == null) {
                    scheduleViewModel.fetchSchedule(group)
                }
            }
        }

        lessonDetailViewModel.lessonDetailLiveData.observe(viewLifecycleOwner) { lesson ->
            if (lesson != null) {
                openDialogForLesson()
            }
        }
    }
    
    private fun updateWidgets() {
        val intent = Intent(requireContext(), ScheduleWidget::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(
                AppWidgetManager.EXTRA_APPWIDGET_IDS,
                AppWidgetManager.getInstance(requireContext())
                    .getAppWidgetIds(ComponentName(requireContext(), ScheduleWidget::class.java))
            )
        }
        requireContext().sendBroadcast(intent)
    }
    
    private fun setupWeekSelector(weekNumbers: List<Int>, group: String) {
        binding.weekButton.setOnClickListener {
            val popup = PopupMenu(requireContext(), binding.weekButton)
            weekNumbers.forEachIndexed { index, number ->
                popup.menu.add(0, number, index, "$number неделя")
            }

            popup.setOnMenuItemClickListener { item ->
                val selectedNumber = weekNumbers[item.itemId]
                loadWeekFragment(weekNumbers.indexOf(selectedNumber), group)
                true
            }

            popup.show()
        }
    }
    
    private fun loadCurrentWeek(weekNumbers: List<Int>, group: String) {
        CurrentTimeObject.currentWeekLiveData.value?.let { currentWeek ->
            var weekNumber = currentWeek

            if (CurrentTimeObject.currentDayLiveData.value == 7) {
                if (weekNumbers.contains(currentWeek + 1)) {
                    weekNumber += 1
                }
            }

            loadWeekFragment(weekNumber, group)
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

    fun openDialogForLesson() {
        if (binding.detail != null) {
            childFragmentManager.commit {
                setReorderingAllowed(true)
                replace(R.id.detail, LessonViewBottomSheetFragment::class.java, null)
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            }
            binding.detailTv?.visibility = View.GONE
        } else {
            val modalBottomSheet = LessonViewBottomSheetFragment()
            modalBottomSheet.show(requireActivity().supportFragmentManager, LessonViewBottomSheetFragment.TAG)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
