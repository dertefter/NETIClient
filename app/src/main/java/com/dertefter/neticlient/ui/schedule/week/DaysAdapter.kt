package com.dertefter.neticlient.ui.schedule.week

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dertefter.neticlient.data.model.schedule.Day
import com.dertefter.neticlient.ui.schedule.week.day.DayFragment

class DaysAdapter(val fragment: WeekFragment) : FragmentStateAdapter(fragment) {

    var group: String = ""
    var dayList: List<Day> = emptyList()
    var weekNumber: Int = 1

    fun updateData(list: List<Day>, group: String?, weekNumber: Int) {
        dayList = list
        this.group = group ?: ""
        this.weekNumber = weekNumber

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
            "${group}_${dayList[position]}".hashCode().toLong()
        } catch (e: Exception) {
            super.getItemId(position)
        }
    }

    override fun containsItem(itemId: Long): Boolean {
        return (0 until itemCount).any {
            getItemId(it) == itemId
        }
    }
}