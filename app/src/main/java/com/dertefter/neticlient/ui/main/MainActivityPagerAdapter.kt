package com.dertefter.neticlient.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class MainActivityPagerAdapter(val fragment: MainActivity) : FragmentStateAdapter(fragment) {

    var list = mutableListOf<Int>()

    fun setData(list: List<Int>) {
        this.list = list.toMutableList()
        notifyDataSetChanged()

    }

    override fun getItemCount(): Int = list.size

    override fun createFragment(position: Int): Fragment {
        return BaseNavFragment().apply {
            arguments = Bundle().apply {
                putInt("NAV_GRAPH", list[position])
            }
        }
    }
}