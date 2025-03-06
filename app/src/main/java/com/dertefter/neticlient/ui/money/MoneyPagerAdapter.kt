package com.dertefter.neticlient.ui.money

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dertefter.neticlient.data.model.sessia_results.SessiaResultSemestr
import com.dertefter.neticlient.ui.money.money_year.MoneyYearFragment

class MoneyPagerAdapter(val fragment: MoneyFragment) : FragmentStateAdapter(fragment) {

    var semestrList = mutableListOf<String>()

    fun setData(newSemestrList: List<String>) {
        semestrList = newSemestrList.toMutableList()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = semestrList.size

    override fun createFragment(position: Int): Fragment {
        return MoneyYearFragment().apply {
            arguments = Bundle().apply {
                putString("YEAR", semestrList[position])
            }
        }
    }

}