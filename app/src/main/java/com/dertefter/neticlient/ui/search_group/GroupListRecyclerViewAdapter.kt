package com.dertefter.neticlient.ui.search_group

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.R

class GroupListRecyclerViewAdapter(
    var items: List<String> = emptyList(),
    val searchGroupBottomSheet: SearchGroupBottomSheet,
) : RecyclerView.Adapter<GroupListRecyclerViewAdapter.GroupItemViewHolder>() {

    fun setData(l: List<String>) {
        val max = if (l.size > 30){
            60
        } else {
            l.size
        }
        items = l.subList(0,max)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group, parent, false)
        return GroupItemViewHolder(view) { selectedGroup -> searchGroupBottomSheet.selectGroup(selectedGroup)
        }
    }

    override fun onBindViewHolder(holder: GroupItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class GroupItemViewHolder(itemView: View, private val onItemClick: (String) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val groupTitle: TextView = itemView.findViewById(R.id.group_title)

        fun bind(item: String) {
            groupTitle.text = item
            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}
