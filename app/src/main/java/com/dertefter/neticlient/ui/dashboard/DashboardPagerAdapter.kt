package com.dertefter.neticlient.ui.dashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dertefter.neticlient.data.model.dashboard.DashboardItem
import com.dertefter.neticlient.data.model.dashboard.DashboardItemType
import com.dertefter.neticlient.ui.profile.ProfileFragment
import com.dertefter.neticlient.ui.schedule.ScheduleFragment
import com.dertefter.neticlient.ui.sessia_schedule.SessiaScheduleFragment

class DashboardPagerAdapter(dashboardFragment: DashboardFragment) : FragmentStateAdapter(dashboardFragment) {

    private var items: List<DashboardItem> = emptyList()

    fun setData(items: List<DashboardItem>) {
        if (this.items != items){
            this.items = items
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = items.size

    override fun createFragment(position: Int): Fragment {
        val item = items[position]
        val fragment = when (item.itemType) {
            DashboardItemType.SESSIA_SCHEDULE -> SessiaScheduleFragment().apply {
                arguments = Bundle().apply { putString("group", item.data) }
            }
            DashboardItemType.SCHEDULE -> ScheduleFragment().apply {
                arguments = Bundle().apply { putString("group", item.data) }
            }
            DashboardItemType.NEWS -> ProfileFragment()
        }
        return fragment
    }
}
