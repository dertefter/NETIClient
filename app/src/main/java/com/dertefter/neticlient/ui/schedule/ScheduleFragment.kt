package com.dertefter.neticlient.ui.schedule

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.CurrentTimeObject
import com.dertefter.neticlient.data.model.schedule.Lesson
import com.dertefter.neticlient.data.model.schedule.Time
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.databinding.FragmentScheduleBinding
import com.dertefter.neticlient.ui.schedule.lesson_view.LessonViewBottomSheetFragment
import com.dertefter.neticlient.ui.schedule.lesson_view.LessonViewViewModel
import com.dertefter.neticlient.ui.schedule.week.DayListPagerAdapter
import com.dertefter.neticlient.ui.search_group.SearchGroupBottomSheet
import com.dertefter.neticlient.ui.settings.SettingsViewModel
import com.dertefter.neticlient.utils.Utils
import com.google.android.material.color.MaterialColors
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!

    private val scheduleViewModel: ScheduleViewModel by activityViewModels()
    private val lessonViewViewModel: LessonViewViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()

    private lateinit var adapter: DayListPagerAdapter




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

        if (savedInstanceState != null && savedInstanceState.getBoolean("STARTED")){
            Log.e("state", "saved")
            return
        }

        lessonViewViewModel.clearData()

        lessonViewViewModel.lessonLiveData.observe(viewLifecycleOwner){ lesson ->
            val time = lessonViewViewModel.timeLiveData.value
            if (lesson != null && time != null){
                openDialogForLesson(lesson, time)
            } else {
                clearDialogForLesson()
            }
        }

        settingsViewModel.insetsViewModel.observe(viewLifecycleOwner){
            binding.appBarLayout.updatePadding(
                top = it[0],
                bottom = 0,
                right = it[2],
                left = it[3]
            )
        }

        binding.appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (verticalOffset < 0){
                Utils.basicAnimationOff(binding.toolbar, false).start()
            } else {
                Utils.basicAnimationOn(binding.toolbar).start()
            }
            Log.e("verticalOffset", verticalOffset.toString())
        }

        val days = listOf("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ")

        if (binding.pager.adapter == null){
            adapter = DayListPagerAdapter(this)
            binding.pager.adapter = adapter
        }

        TabLayoutMediator(binding.daysTabLayout, binding.pager) { tab, position ->
            tab.text = days[position]
        }.attach()

        settingsViewModel.insetsViewModel.observe(viewLifecycleOwner){
            binding.appBarLayout.updatePadding(
                top = it[0],
                right = it[2],
                left = it[3]
            )
        }


        scheduleViewModel.selectedGroupLiveData.observe(viewLifecycleOwner) { group ->
            if (group.isNullOrEmpty()) {
                binding.noGroup.visibility = View.VISIBLE
            } else {
                binding.noGroup.visibility = View.GONE
                binding.groupButton.text = group
                if (adapter.group != group){
                    scheduleViewModel.fetchWeekNumberList(group)
                    scheduleViewModel.fetchSchedule(group)
                }

            }
        }

        scheduleViewModel.weekNumberListLiveData.observe(viewLifecycleOwner) { weekListResponse ->
            Log.e("weekNumberListLiveData.observe", weekListResponse.toString())
            if (weekListResponse.responseType == ResponseType.SUCCESS) {
                weekListResponse.data?.let { data ->
                    val items = (data as List<Int>).map { "Неделя $it" }
                    val autoCompleteTextView = binding.weeksMenu.editText as? MaterialAutoCompleteTextView
                    autoCompleteTextView?.setSimpleItems(items.toTypedArray())

                    autoCompleteTextView?.setOnItemClickListener { _, _, position, _ ->
                        scheduleViewModel.selectedGroupLiveData.value?.let { group ->
                            if (adapter.group != group || adapter.dayList != days || adapter.weekNumber != position + 1){
                                adapter.setData(days, group, position + 1)
                            }

                            if (CurrentTimeObject.currentDayLiveData.value != null){
                                if (CurrentTimeObject.currentWeekLiveData.value != null){
                                    val currentDay = CurrentTimeObject.currentDayLiveData.value!!
                                    val currentWeek = CurrentTimeObject.currentWeekLiveData.value!!
                                    if ((currentDay - 1) in 0..5 && (position + 1) == currentWeek) {
                                        binding.pager.currentItem = currentDay
                                    }
                                }

                            }
                            autoCompleteTextView.dismissDropDown()
                        }
                    }

                    CurrentTimeObject.currentWeekLiveData.observe(viewLifecycleOwner) { currentWeek ->
                        scheduleViewModel.selectedGroupLiveData.value?.let { group ->
                            if (adapter.group != group || adapter.dayList != days || adapter.weekNumber != currentWeek){
                                adapter.setData(days, group, currentWeek)
                            }


                            val autoCompleteTextView = binding.weeksMenu.editText as? MaterialAutoCompleteTextView
                            val weekItems = autoCompleteTextView?.adapter
                            if (weekItems != null && currentWeek - 1 < weekItems.count) {
                                autoCompleteTextView.setText(weekItems.getItem(currentWeek - 1).toString(), false)

                                if (CurrentTimeObject.currentDayLiveData.value != null){
                                    val currentDay = CurrentTimeObject.currentDayLiveData.value!!
                                    if ((currentDay - 1) in 0..5 ) {
                                        binding.pager.currentItem = currentDay - 1
                                    } else if ((currentDay - 1) == 6 && currentWeek < weekItems.count){
                                        if (adapter.group != group || adapter.dayList != days || adapter.weekNumber != currentWeek + 1){
                                            adapter.setData(days, group, currentWeek + 1)
                                        }

                                        autoCompleteTextView.setText(weekItems.getItem(currentWeek).toString(), false)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (weekListResponse.responseType == ResponseType.ERROR){
            }
            if (weekListResponse.responseType == ResponseType.LOADING && adapter.dayList.isEmpty()){
                binding.skeleton?.visibility = View.VISIBLE
            } else {
                binding.skeleton?.visibility = View.GONE
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

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun blinkPager() {
        Utils.basicAnimationOn(binding.pager).start()
    }

    fun openDialogForLesson(lesson: Lesson, timeItem: Time){
        val isTab = resources.getBoolean(R.bool.isTab)
        if (!isTab) {
            val modalBottomSheet = LessonViewBottomSheetFragment()
            modalBottomSheet.show(requireActivity().supportFragmentManager, LessonViewBottomSheetFragment.TAG)
        } else {
        }
    }

    fun clearDialogForLesson(){
        val isTab = resources.getBoolean(R.bool.isTab)
        if (isTab) {

        }
    }
}



