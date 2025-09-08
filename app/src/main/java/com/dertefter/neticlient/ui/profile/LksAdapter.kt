package com.dertefter.neticlient.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.databinding.ItemLksBinding
import com.dertefter.neticlient.databinding.ItemLksSelectedBinding
import com.dertefter.neticore.features.user_detail.model.lks

class LksAdapter(private var items: List<lks> = emptyList(), private val onItemClick: (lks) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SELECTED = 1
    private val TYPE_NORMAL = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SELECTED) {
            val binding = ItemLksSelectedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            SelectedViewHolder(binding)
        } else {
            val binding = ItemLksBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            NormalViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (holder is NormalViewHolder) {
            holder.bind(item)
        } else if (holder is SelectedViewHolder) {
            holder.bind(item)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return if (items[position].isSelected) TYPE_SELECTED else TYPE_NORMAL
    }

    inner class NormalViewHolder(private val binding: ItemLksBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: lks) {
            binding.title.text = item.title
            binding.subtitle.text = item.subtitle
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    inner class SelectedViewHolder(private val binding: ItemLksSelectedBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: lks) {
            binding.title.text = item.title
            binding.subtitle.text = item.subtitle
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    // Метод для обновления элементов в адаптере
    fun updateItems(newItems: List<lks>) {
        this.items = newItems
        notifyDataSetChanged()  // Обновляем адаптер
    }
}


