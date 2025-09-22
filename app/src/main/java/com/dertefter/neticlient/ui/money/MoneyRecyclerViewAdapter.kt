package com.dertefter.neticlient.ui.money

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.databinding.ItemMoneyBinding
import com.dertefter.neticore.features.money.model.MoneyItem

class MoneyRecyclerViewAdapter :
    ListAdapter<MoneyItem, MoneyRecyclerViewAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private val binding: ItemMoneyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MoneyItem) {
            binding.title.text = item.title
            binding.text.text = item.text
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMoneyBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    object DiffCallback : DiffUtil.ItemCallback<MoneyItem>() {
        override fun areItemsTheSame(oldItem: MoneyItem, newItem: MoneyItem): Boolean {
            return oldItem.title+oldItem.text == newItem.title+newItem.text
        }

        override fun areContentsTheSame(oldItem: MoneyItem, newItem: MoneyItem): Boolean {
            return oldItem == newItem
        }
    }
}
