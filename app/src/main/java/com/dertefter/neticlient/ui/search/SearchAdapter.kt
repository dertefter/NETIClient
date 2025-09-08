package com.dertefter.neticlient.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.databinding.ItemSearchBinding
import com.dertefter.neticlient.databinding.ItemSearchDistBinding
import com.dertefter.neticlient.databinding.ItemSearchGroupBinding
import com.dertefter.neticlient.databinding.ItemSearchPersonBinding

class SearchAdapter : ListAdapter<SearchItem, RecyclerView.ViewHolder>(SearchDiffCallback) {


    var onGroupClick: ((SearchItem.GroupItem) -> Unit)? = null
    var onPersonClick: ((SearchItem.PersonItem) -> Unit)? = null
    var onNavClick: ((SearchItem.NavDestinationItem) -> Unit)? = null

    companion object {
        private const val TYPE_GROUP = 0
        private const val TYPE_PERSON = 1
        private const val TYPE_NAV = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is SearchItem.GroupItem -> TYPE_GROUP
            is SearchItem.PersonItem -> TYPE_PERSON
            is SearchItem.NavDestinationItem -> TYPE_NAV
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            TYPE_GROUP -> {
                val binding = ItemSearchGroupBinding.inflate(layoutInflater, parent, false)
                GroupViewHolder(binding)
            }
            TYPE_PERSON -> {
                val binding = ItemSearchPersonBinding.inflate(layoutInflater, parent, false)
                PersonViewHolder(binding)
            }
            TYPE_NAV -> {
                val binding = ItemSearchDistBinding.inflate(layoutInflater, parent, false)
                NavDestinationViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown viewType $viewType")
        }
    }


    inner class GroupViewHolder(private val binding: ItemSearchGroupBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SearchItem.GroupItem) {
            binding.title.text = item.name
            binding.root.setOnClickListener { onGroupClick?.invoke(item) }
        }
    }

    inner class PersonViewHolder(private val binding: ItemSearchPersonBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SearchItem.PersonItem) {
            binding.title.text = item.name
            binding.root.setOnClickListener { onPersonClick?.invoke(item) }
        }
    }

    inner class NavDestinationViewHolder(private val binding: ItemSearchDistBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SearchItem.NavDestinationItem) {
            binding.title.text = item.dist.label
            binding.root.setOnClickListener { onNavClick?.invoke(item) }
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is SearchItem.GroupItem -> (holder as GroupViewHolder).bind(item)
            is SearchItem.PersonItem -> (holder as PersonViewHolder).bind(item)
            is SearchItem.NavDestinationItem -> (holder as NavDestinationViewHolder).bind(item)
        }
    }


    object SearchDiffCallback : DiffUtil.ItemCallback<SearchItem>() {
        override fun areItemsTheSame(oldItem: SearchItem, newItem: SearchItem): Boolean {
            return oldItem == newItem // либо сравнивать по id
        }

        override fun areContentsTheSame(oldItem: SearchItem, newItem: SearchItem): Boolean {
            return oldItem == newItem
        }
    }
}
