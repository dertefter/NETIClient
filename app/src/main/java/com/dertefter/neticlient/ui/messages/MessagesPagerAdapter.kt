package com.dertefter.neticlient.ui.messages

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dertefter.neticlient.ui.main.BaseNavFragment

class MessagesPagerAdapter(val fragment: MessagesFragment) : FragmentStateAdapter(fragment) {

    private var items: List<String> = emptyList()

    fun setData(items: List<String>) {
        if (this.items != items){
            this.items = items
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = items.size

    override fun createFragment(position: Int): Fragment {
        if (position == 0){
            return MessagesTabFragment().apply {
                arguments = Bundle().apply { putString("tab", "tabs1-messages") }
            }
        } else if (position == 1){
            return MessagesTabFragment().apply {
                arguments = Bundle().apply { putString("tab", "tabs2-messages") }
            }
        } else {
            return BaseNavFragment()
        }
    }
}
