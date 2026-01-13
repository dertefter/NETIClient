package com.dertefter.neticlient.ui.schedule

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.AppBarEdgeToEdge
import com.dertefter.neticlient.databinding.FragmentScheduleBinding
import com.dertefter.neticlient.ui.schedule.lesson_view.LessonViewBottomSheetFragment
import com.dertefter.neticlient.ui.search_group.SearchGroupBottomSheet
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.features.schedule.model.Day
import com.dertefter.neticore.features.schedule.model.Lesson
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class ScheduleFragment : Fragment() {

    @Inject
    lateinit var netiCore: NETICore

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!

    private val scheduleViewModel: ScheduleViewModel by activityViewModels()

    private var adapter: DaysAdapter? = null

    val formatter =  DateTimeFormatter.ofPattern("E", Locale("ru"))

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appbar.addOnOffsetChangedListener(AppBarEdgeToEdge( binding.appbar))

        setupAdapter()

        binding.groupButton.setOnClickListener { openGroupSearchDialog() }

        binding.daysPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.daysPager) { tab, position ->
            val day = adapter?.currentList?.getOrNull(position)
            if (day != null) {
                val dayName = day.getDate()?.format(formatter)?.uppercase() ?: day.dayName.toString()
                val dayOfMonth = day.getDate()?.dayOfMonth.toString()
                tab.text = "$dayName\n$dayOfMonth"
            }
        }.attach()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val position = tab.position
                val day: Day = adapter?.currentList?.getOrNull(position) ?: return

                val month = day.getDate()
                    ?.month
                    ?.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault())
                    ?.replaceFirstChar { it.titlecase(Locale.getDefault()) }
                    ?: ""

                binding.mTextView.text = month

                binding.weekTextView.text = day.weekNumber.toString() +" "+ getString(R.string.week)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })


        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }


        collectingGroup()

        collectingSchedule()
    }

    private fun setupAdapter() {
        val personDetailFeature = netiCore.personDetailFeature

        adapter = DaysAdapter(
            lifecycleOwner = viewLifecycleOwner,
            onLessonClick = { lesson ->
                LessonViewBottomSheetFragment().apply {
                    arguments = Bundle().apply { putParcelable("lesson", lesson) }
                }.show(parentFragmentManager, "LessonDetail")
            },
            personDetailFeature = personDetailFeature
        )
    }

    private fun collectingGroup() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                scheduleViewModel.currentGroup.collect { currentGroup ->
                    if (currentGroup != null) {
                        binding.groupButton.text = currentGroup
                        scheduleViewModel.updateScheduleForGroup(currentGroup)
                    }
                    Log.e("collectingGroup", "currentGroup: $currentGroup")
                }
            }
        }
    }

    private fun collectingSchedule() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                scheduleViewModel.schedule.collect { schedule ->
                    val days = schedule?.getAllDays() ?: return@collect
                    val currentDay = schedule.findNextDayWithLessonsAfter(
                        date = LocalDate.now(),
                        time = LocalTime.now()
                    )

                    adapter?.submitList(days)
                    val currentDayIndex = days.indexOfFirst { it.date == currentDay?.date }

                    Log.e("collectingSchedule", "currentDayIndex: $currentDayIndex")
                    adapter?.submitList(days) {
                        if (currentDayIndex != -1 && binding.daysPager.currentItem != currentDayIndex) {
                            binding.daysPager.post {
                                binding.daysPager.setCurrentItem(currentDayIndex, false)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.daysPager.adapter = null
        _binding = null

    }

    private fun openGroupSearchDialog() {
        SearchGroupBottomSheet().show(requireActivity().supportFragmentManager, SearchGroupBottomSheet.TAG)
    }

}