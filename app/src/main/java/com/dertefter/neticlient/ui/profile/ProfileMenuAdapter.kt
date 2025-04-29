package com.dertefter.neticlient.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticlient.data.model.profile_menu.ProfileMenuItem
import com.dertefter.neticlient.ui.profile.profile_dialog.ProfileDialogFragment

class ProfileMenuAdapter(
    private var items: List<ProfileMenuItem> = emptyList(),
    private val fragment: ProfileDialogFragment
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class ItemDisabledViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val icon: ImageView = view.findViewById(R.id.icon)
        private val title: TextView = view.findViewById(R.id.title)
        fun bind(item: ProfileMenuItem) {
            title.text = item.title
            item.iconResId?.let { icon.setImageResource(it) }
        }
    }

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val icon: ImageView = view.findViewById(R.id.icon)
        private val title: TextView = view.findViewById(R.id.title)

        fun bind(item: ProfileMenuItem) {
            title.text = item.title
            item.iconResId?.let { icon.setImageResource(it) }
            itemView.setOnClickListener {
                item.navigateToDestination?.let { it1 ->
                    fragment.findNavController().navigate(
                        it1,
                        null,
                        Utils.getNavOptions(),
                    )
                    fragment.dismiss()
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position].enabled) {0} else {1}
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_profile_menu, parent, false)
                ItemViewHolder(view)
            }
            1 -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_profile_menu_disabled, parent, false)
                ItemDisabledViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown viewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is ItemDisabledViewHolder -> holder.bind(item)
            is ItemViewHolder -> holder.bind(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<ProfileMenuItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
