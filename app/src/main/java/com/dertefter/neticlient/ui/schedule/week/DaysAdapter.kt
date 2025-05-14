package com.dertefter.neticlient.ui.schedule.week

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dertefter.neticlient.data.model.schedule.Day
import com.dertefter.neticlient.data.model.schedule.Week
import com.dertefter.neticlient.ui.schedule.week.day.DayFragment

class DaysAdapter(val fragment: WeekFragment) : FragmentStateAdapter(fragment) {

    var dayList: List<Day> = emptyList()

    fun updateData(week: Week) {
        dayList = week.days
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = dayList.size

    override fun createFragment(position: Int): Fragment {
        return DayFragment().apply {
            arguments = Bundle().apply {
                putParcelable("DAY", dayList[position])
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return try {
            "${dayList[position]}".hashCode().toLong()
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