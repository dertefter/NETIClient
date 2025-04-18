package com.dertefter.neticlient.ui.schedule

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dertefter.neticlient.ui.schedule.ScheduleFragment
import com.dertefter.neticlient.ui.schedule.week.day.DayFragment
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticlient.data.model.schedule.Week
import com.dertefter.neticlient.ui.schedule.week.WeekFragment

class WeeksAdapter(val fragment: ScheduleFragment) : FragmentStateAdapter(fragment) {

    var group: String = ""

    var weeks: List<Week> = emptyList()

    fun updateData(list: List<Week>, group: String?) {
        this.weeks = list
        this.group = group ?: ""
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = weeks.size

    override fun createFragment(position: Int): Fragment {
        return WeekFragment().apply {
            arguments = Bundle().apply {
                putInt("WEEK_NUMBER", position + 1)
                putString("GROUP", group)
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return try {
            "${weeks[position]}_${group}".hashCode().toLong()
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