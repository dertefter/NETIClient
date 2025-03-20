package com.dertefter.neticlient.ui.schedule.week.day

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.item_decoration.GridSpacingItemDecoration
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

        val adapter = TimesAdapter(fragment = this)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.addItemDecoration(GridSpacingItemDecoration(requireContext(), 1, R.dimen.margin_min))

        settingsViewModel.legendaryCardsState.observe(viewLifecycleOwner){
            adapter.putLegendary(it)
        }


        scheduleViewModel.getScheduleLiveData(group!!).observe(viewLifecycleOwner){
            if (it.responseType == ResponseType.SUCCESS){
                if (it.data != null){
                    val schedule = it.data as Schedule
                    Log.e("schedule", (schedule.days[dayNumber!! - 1].times).toString())
                    adapter.setData(schedule.days[dayNumber!! - 1].times, weekNumber!!, dayNumber)
                }
            }
        }

    }

    fun setVisibleNoLessons(b: Boolean){
        binding.noLessons.visibility = if (b) View.VISIBLE else View.GONE
    }

}