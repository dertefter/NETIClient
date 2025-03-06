package com.dertefter.neticlient.ui.search_group

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.R

class GroupListHistoryRecyclerViewAdapter(
    var items: List<String> = emptyList(),
    val searchGroupBottomSheet: SearchGroupBottomSheet,
) : RecyclerView.Adapter<GroupListHistoryRecyclerViewAdapter.GroupItemViewHolder>() {

    fun setData(l: List<String>) {
        items = l
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group_history, parent, false)
        return GroupItemViewHolder(view, searchGroupBottomSheet)
    }

    override fun onBindViewHolder(holder: GroupItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class GroupItemViewHolder(itemView: View, val searchGroupBottomSheet: SearchGroupBottomSheet) :
        RecyclerView.ViewHolder(itemView) {
        private val groupTitle: TextView = itemView.findViewById(R.id.group_title)
        private val remove: ImageView = itemView.findViewById(R.id.remove)
        fun bind(item: String) {
            groupTitle.text = item
            itemView.setOnClickListener {
                searchGroupBottomSheet.selectGroup(item)
            }
            remove.setOnClickListener {
                searchGroupBottomSheet.remove(item)
            }
        }
    }
}
