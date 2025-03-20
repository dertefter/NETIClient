package com.dertefter.neticlient.ui.schedule.week

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dertefter.neticlient.ui.schedule.ScheduleFragment
import com.dertefter.neticlient.ui.schedule.week.day.DayFragment
import com.dertefter.neticlient.common.utils.Utils

class DayListPagerAdapter(val fragment: ScheduleFragment) : FragmentStateAdapter(fragment) {

    var group: String = ""
    var dayList: List<String> = emptyList()
    var weekNumber: Int = 1

    private var lastValidGroup: String = ""
    private var lastValidWeek: Int = 1
    private var lastValidDays: List<String> = emptyList()

    fun setData(list: List<String>, group: String?, weekNumber: Int) {
        dayList = list
        this.group = group ?: ""
        this.weekNumber = weekNumber

        if (!group.isNullOrEmpty() && list.isNotEmpty()) {
            lastValidGroup = group
            lastValidWeek = weekNumber
            lastValidDays = list
            fragment.blinkPager()
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = dayList.size

    override fun createFragment(position: Int): Fragment {
        return DayFragment().apply {
            arguments = Bundle().apply {
                putInt("DAY_NUMBER", position + 1)
                putInt("WEEK_NUMBER", weekNumber)
                putString("GROUP", group)
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return try {
            // Используем последние валидные данные для генерации ID
            "${lastValidGroup}_${lastValidWeek}_${lastValidDays[position]}".hashCode().toLong()
        } catch (e: Exception) {
            // Возвращаем fallback ID при ошибке
            super.getItemId(position)
        }
    }

    override fun containsItem(itemId: Long): Boolean {
        // Проверяем все возможные ID для текущих данных
        return (0 until itemCount).any {
            getItemId(it) == itemId
        }
    }
}