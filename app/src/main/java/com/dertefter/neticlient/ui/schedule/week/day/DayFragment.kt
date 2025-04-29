package com.dertefter.neticlient.ui.schedule.week.day

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertefter.neticlient.data.model.schedule.LessonDetail
import com.dertefter.neticlient.data.model.schedule.Schedule
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.databinding.FragmentDayBinding
import com.dertefter.neticlient.ui.schedule.ScheduleViewModel
import com.dertefter.neticlient.ui.schedule.lesson_view.LessonDetailViewModel
import com.dertefter.neticlient.ui.settings.SettingsViewModel

class DayFragment : Fragment() {

    lateinit var binding: FragmentDayBinding
    private val scheduleViewModel: ScheduleViewModel by activityViewModels()
    private val lessonViewViewModel: LessonDetailViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDayBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun showLessonInfo(lessonDetail: LessonDetail){
        lessonViewViewModel.setData(lessonDetail)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val weekNumber = arguments?.getInt("WEEK_NUMBER")
        val dayNumber = arguments?.getInt("DAY_NUMBER")
        val group = arguments?.getString("GROUP")

        ViewCompat.setOnApplyWindowInsetsListener(binding.recyclerView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = insets.bottom)
            WindowInsetsCompat.CONSUMED
        }

        val adapter = TimesAdapter(fragment = this)
        binding.recyclerView.adapter = adapter
        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.layoutManager = layoutManager


        settingsViewModel.legendaryCardsState.observe(viewLifecycleOwner){
            adapter.putLegendary(it)
        }


        scheduleViewModel.getScheduleLiveData(group!!).observe(viewLifecycleOwner){ response ->
            if (response.responseType == ResponseType.SUCCESS){
                if (response.data != null){
                    val schedule = response.data as Schedule
                    val week = schedule.weeks.find{it.weekNumber == weekNumber}!!
                    val day = week.days.find{it.dayNumber == dayNumber}!!
                    val times = day.times
                    adapter.setData(
                        times,
                        weekNumber = week.weekNumber,
                        dayNumber = day.dayNumber,
                    )
                }
            }
        }

    }

    fun setVisibleNoLessons(b: Boolean){
        binding.noLessons.visibility = if (b) View.VISIBLE else View.GONE
    }

}