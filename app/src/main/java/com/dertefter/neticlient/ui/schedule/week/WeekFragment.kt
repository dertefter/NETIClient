package com.dertefter.neticlient.ui.schedule.week

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.CurrentTimeObject
import com.dertefter.neticlient.data.model.schedule.LessonDetail
import com.dertefter.neticlient.data.model.schedule.Schedule
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.databinding.FragmentWeekBinding
import com.dertefter.neticlient.ui.schedule.ScheduleFragment
import com.dertefter.neticlient.ui.schedule.ScheduleViewModel
import com.dertefter.neticlient.ui.schedule.lesson_view.LessonDetailViewModel
import com.google.android.material.color.MaterialColors
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.time.format.DateTimeFormatter
import java.util.Locale

class WeekFragment : Fragment() {

    lateinit var binding: FragmentWeekBinding
    private val scheduleViewModel: ScheduleViewModel by activityViewModels()
    private val lessonViewViewModel: LessonDetailViewModel by activityViewModels()

    private lateinit var adapter: DaysAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWeekBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun showLessonInfo(lessonDetail: LessonDetail){
        lessonViewViewModel.setData(lessonDetail)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val weekNumber = arguments?.getInt("WEEK_NUMBER")!!
        val group = arguments?.getString("GROUP")!!

        adapter = DaysAdapter(this)
        binding.pager.adapter = adapter
        val daysTabLayout = (parentFragment as ScheduleFragment).binding.daysTabLayout
        val yearAndMounth = (parentFragment as ScheduleFragment).binding.yearAndMounth

        scheduleViewModel.getScheduleLiveData(group).observe(viewLifecycleOwner){ response ->
            if (response.responseType == ResponseType.SUCCESS){
                val schedule = response.data as Schedule
                val weekInFragment = schedule.weeks.find { it.weekNumber == weekNumber}!!
                val days = weekInFragment.days
                adapter.updateData(days, group, weekNumber)

                val inflater = LayoutInflater.from(context)

                TabLayoutMediator(daysTabLayout, binding.pager) { tab, position ->

                    val day = days[position]
                    val customTabView = inflater.inflate(R.layout.day_tab, null)

                    val dateTextView = customTabView.findViewById<TextView>(R.id.dayDate)
                    val dayNameTextView = customTabView.findViewById<TextView>(R.id.dayName)

                    val formatter = DateTimeFormatter.ofPattern("E", Locale.getDefault())

                    dateTextView.text = day.getDate().dayOfMonth.toString()
                    dayNameTextView.text = day.getDate().format(formatter) ?: day.dayName

                    tab.customView = customTabView

                }.attach()

                daysTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab?) {

                        val date = days[tab!!.position].getDate()
                        val formatter = DateTimeFormatter.ofPattern("LLLL, yyyy", Locale("ru"))
                        val formattedDate = date.format(formatter).replaceFirstChar { it.uppercaseChar() }
                        yearAndMounth.text = formattedDate

                        val customView = tab.customView
                        customView?.findViewById<TextView>(R.id.dayDate)?.setTextColor(
                            MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorOnPrimaryContainer)
                        )
                        customView?.findViewById<TextView>(R.id.dayName)?.setTextColor(
                            MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorOnPrimaryContainer)
                        )
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab?) {
                        val customView = tab?.customView
                        customView?.findViewById<TextView>(R.id.dayDate)?.setTextColor(
                            MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorOnSurface)
                        )
                        customView?.findViewById<TextView>(R.id.dayName)?.setTextColor(
                            MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorOnSurface)
                        )
                    }

                    override fun onTabReselected(tab: TabLayout.Tab?) {

                        val date = days[tab!!.position].getDate()
                        val formatter = DateTimeFormatter.ofPattern("LLLL, yyyy", Locale("ru"))
                        val formattedDate = date.format(formatter).replaceFirstChar { it.uppercaseChar() }
                        yearAndMounth.text = formattedDate

                        val customView = tab.customView
                        customView?.findViewById<TextView>(R.id.dayDate)?.setTextColor(
                            MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorOnPrimaryContainer)
                        )
                        customView?.findViewById<TextView>(R.id.dayName)?.setTextColor(
                            MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorOnPrimaryContainer)
                        )
                    }
                })

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
                        val date = days[position].getDate()
                        val formatter = DateTimeFormatter.ofPattern("LLLL, yyyy", Locale("ru"))
                        val formattedDate = date.format(formatter).replaceFirstChar { it.uppercaseChar() }
                        yearAndMounth.text = formattedDate
                    }

                    override fun onPageScrollStateChanged(state: Int) {
                        super.onPageScrollStateChanged(state)
                    }
                })

                if (CurrentTimeObject.currentDayLiveData.value != null && CurrentTimeObject.currentDayLiveData.value!! < 7){
                    daysTabLayout.getTabAt(CurrentTimeObject.currentDayLiveData.value!! - 1)?.select()
                }else{
                    daysTabLayout.getTabAt(0)?.select()
                }
            }
        }


    }

    fun setVisibleNoLessons(b: Boolean){
        //binding.noLessons.visibility = if (b) View.VISIBLE else View.GONE
    }

}