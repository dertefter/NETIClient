package com.dertefter.neticlient.ui.schedule

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.viewpager2.widget.ViewPager2
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticlient.data.model.CurrentTimeObject
import com.dertefter.neticlient.data.model.schedule.Schedule
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.databinding.FragmentScheduleBinding
import com.dertefter.neticlient.ui.schedule.lesson_view.LessonDetailViewModel
import com.dertefter.neticlient.ui.schedule.lesson_view.LessonViewBottomSheetFragment
import com.dertefter.neticlient.ui.search_group.SearchGroupBottomSheet
import com.dertefter.neticlient.ui.settings.SettingsViewModel
import com.dertefter.neticlient.widgets.schedule_widget.ScheduleWidget
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    val binding get() = _binding!!

    private val scheduleViewModel: ScheduleViewModel by activityViewModels()
    private val lessonDetailViewModel: LessonDetailViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()

    private lateinit var adapter: WeeksAdapter




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("STARTED", true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingsViewModel.insetsViewModel.observe(viewLifecycleOwner){
            val orientation = resources.configuration.orientation
            if (orientation == Configuration.ORIENTATION_PORTRAIT){
                binding.appBarLayout.updatePadding(
                    top = it[0],
                    bottom = 0,
                    right = it[2],
                    left = it[3]
                )
            }else{
                binding.appBarLayout.updatePadding(
                    top = 0,
                    bottom = 0,
                    right = 0,
                    left = 0
                )
            }

        }


        binding.pager.isUserInputEnabled = false
        binding.appBarLayout.setLiftable(true)
        binding.appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val totalScrollRange = appBarLayout.totalScrollRange
            val alpha = 1f - (-verticalOffset.toFloat() / totalScrollRange)
            binding.yearAndMounth.alpha = alpha
            binding.weekGroupContainer.alpha = alpha

            val dayTabsPaddingH = resources.getDimension(R.dimen.margin)
            val dayTabsPaddingV = resources.getDimension(R.dimen.margin_min)

            val newPaddingHorizontal = dayTabsPaddingH - (dayTabsPaddingH - dayTabsPaddingH * alpha)

            val newPaddingVertical = dayTabsPaddingH - (dayTabsPaddingH - dayTabsPaddingH * alpha) * 0.5

            binding.daysTabsContainer?.updatePadding(left = newPaddingHorizontal.toInt(), right = newPaddingHorizontal.toInt(), bottom = newPaddingVertical.toInt(), top = newPaddingVertical.toInt())
            if (verticalOffset < 0) {
                binding.appBarLayout.isLifted = true
            } else {
                binding.appBarLayout.isLifted = false
            }
        }


        if (binding.pager.adapter == null){
            adapter = WeeksAdapter(this)
            binding.pager.adapter = adapter
        }


        scheduleViewModel.selectedGroupLiveData.observe(viewLifecycleOwner) { group ->

            val intent = Intent(requireContext(), ScheduleWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
                    AppWidgetManager.getInstance(requireContext())
                        .getAppWidgetIds(ComponentName(requireContext(), ScheduleWidget::class.java))
                )
            }
            requireContext().sendBroadcast(intent)


            if (group.isNullOrEmpty()) {
                binding.appBarLayout.visibility = View.GONE
                binding.noGroup.visibility = View.VISIBLE
            } else {
                binding.appBarLayout.visibility = View.VISIBLE
                binding.noGroup.visibility = View.GONE
                binding.groupButton.text = group
                if (adapter.group != group){
                    scheduleViewModel.getScheduleLiveData(group).observe(viewLifecycleOwner){ it ->
                        if (it.responseType == ResponseType.SUCCESS && it.data != null){
                            Utils.basicAnimationOff(binding.skeleton, true).start()
                            Utils.basicAnimationOn(binding.pager).start()
                            val schedule = it.data as Schedule?
                            val weeks = schedule?.weeks
                            val weekNumbers: List<Int> = weeks!!.map { it.weekNumber }
                            if (!weeks.isNullOrEmpty()){
                                adapter.updateData(schedule.weeks, group)

                                binding.weekButton.setOnClickListener {
                                    val popup = PopupMenu(requireContext(), binding.weekButton)
                                    weekNumbers.forEachIndexed { index, number ->
                                        popup.menu.add(0, index, index, "$number неделя")
                                    }

                                    popup.setOnMenuItemClickListener { item ->
                                        val selectedNumber = weekNumbers[item.itemId]
                                        binding.pager.setCurrentItem(weekNumbers.indexOf(selectedNumber), false)
                                        true
                                    }

                                    popup.show()
                                }

                                binding.pager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
                                    override fun onPageScrolled(
                                        position: Int,
                                        positionOffset: Float,
                                        positionOffsetPixels: Int
                                    ) {
                                        super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                                    }

                                    override fun onPageSelected(position: Int) {
                                        super.onPageSelected(position)
                                        binding.weekButton.text = weekNumbers[position].toString() + " неделя"
                                    }

                                    override fun onPageScrollStateChanged(state: Int) {
                                        super.onPageScrollStateChanged(state)
                                    }
                                })

                                if (CurrentTimeObject.currentWeekLiveData.value != null){
                                    var weekNumber = CurrentTimeObject.currentWeekLiveData.value?: 1
                                    if (CurrentTimeObject.currentDayLiveData.value == 7){
                                        if (weekNumbers.indexOf(weekNumber + 1) != -1){
                                            weekNumber += 1
                                        }
                                    }
                                    binding.pager.setCurrentItem(weekNumbers.indexOf(weekNumber), false)
                                }
                            }

                        }
                        else{
                            Utils.basicAnimationOn(binding.skeleton).start()
                            Utils.basicAnimationOff(binding.pager).start()
                        }

                    }

                    scheduleViewModel.fetchSchedule(group)



                }

            }
        }

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

        if (scheduleViewModel.selectedGroupLiveData.value == null) {
            scheduleViewModel.getSelectedGroup()
        }
        if (CurrentTimeObject.currentWeekLiveData.value == null) {
            scheduleViewModel.fetchCurrentWeekNumber()
        }

        lessonDetailViewModel.lessonDetailLiveData.observe(viewLifecycleOwner){
            if (it != null){
                openDialogForLesson()
            }
        }

    }

    fun blinkPager() {
        Utils.basicAnimationOn(binding.pager).start()
    }

    fun openDialogForLesson(){
        if (binding.detail != null){
            childFragmentManager.commit {
                setReorderingAllowed(true)
                replace(R.id.detail, LessonViewBottomSheetFragment::class.java, null)
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            }
            binding.detailTv?.visibility = View.GONE
        }else{
            val modalBottomSheet = LessonViewBottomSheetFragment()
            modalBottomSheet.show(requireActivity().supportFragmentManager, LessonViewBottomSheetFragment.TAG)
        }

    }


}



