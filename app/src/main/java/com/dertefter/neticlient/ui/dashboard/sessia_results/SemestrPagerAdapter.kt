package com.dertefter.neticlient.ui.dashboard.sessia_results

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dertefter.neticlient.data.model.sessia_results.SessiaResultSemestr

class SemestrPagerAdapter(val fragment: Fragment) : FragmentStateAdapter(fragment) {

    var semestrList = mutableListOf<SessiaResultSemestr>()

    fun setData(newSemestrList: List<SessiaResultSemestr>) {
        if (semestrList != newSemestrList){
            semestrList = newSemestrList.toMutableList()
            notifyDataSetChanged()
        }
    }

    fun getItem (position: Int): SessiaResultSemestr {
        return semestrList[position]
    }

    override fun getItemCount(): Int = semestrList.size

    override fun createFragment(position: Int): Fragment {
        return SemestrFragment().apply {
            arguments = Bundle().apply {
                putParcelable("SEMESTR", semestrList[position])
            }
        }
    }

}