package com.dertefter.neticlient.ui.dashboard.dashboard_tune

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.dashboard.DashboardItem
import com.dertefter.neticlient.databinding.FragmentDashboardTuneBinding
import com.dertefter.neticlient.ui.dashboard.DashboardFragment
import com.dertefter.neticlient.ui.dashboard.DashboardViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Collections

class DashboardTuneRecyclerViewAdapter(
    private val fragment: DashboardTuneFragment
) : RecyclerView.Adapter<DashboardTuneRecyclerViewAdapter.ItemViewHolder>() {

    private var items = mutableListOf<DashboardItem>()

    fun setItems(newItems: List<DashboardItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val data: TextView = itemView.findViewById(R.id.data)
        val removeButton: Button = itemView.findViewById(R.id.remove_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dashboard_settings, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.title.text = items[position].itemType.name
        holder.data.text = items[position].data.toString()
        if (holder.data.text.isNotEmpty()){
            holder.data.visibility = View.VISIBLE
        } else {
            holder.data.visibility = View.GONE
        }
        holder.removeButton.setOnClickListener {
            fragment.removeItem(items[position])
        }
    }

    override fun getItemCount(): Int = items.size

    fun moveItem(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(items, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(items, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }
}
