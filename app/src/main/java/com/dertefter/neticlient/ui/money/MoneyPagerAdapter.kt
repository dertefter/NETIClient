package com.dertefter.neticlient.ui.money

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dertefter.neticlient.ui.money.money_year.MoneyYearFragment

class MoneyPagerAdapter(fragment: MoneyFragment) : FragmentStateAdapter(fragment) {

    private var semestrList = mutableListOf<String>()

    fun getItem(position: Int): String {
        return semestrList[position]
    }

    fun setData(newSemestrList: List<String>) {
        val diffCallback = SemestrDiffCallback(this.semestrList, newSemestrList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.semestrList.clear()
        this.semestrList.addAll(newSemestrList)
        diffResult.dispatchUpdatesTo(this)
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

class SemestrDiffCallback(
    private val oldList: List<String>,
    private val newList: List<String>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}