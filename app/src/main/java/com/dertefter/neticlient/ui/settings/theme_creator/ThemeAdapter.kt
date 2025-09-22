package com.dertefter.neticlient.ui.settings.theme_creator

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticlient.databinding.ItemThemeBinding

class ThemeAdapter(
    private val onThemeSelected: (Int) -> Unit
) : RecyclerView.Adapter<ThemeAdapter.ThemeViewHolder>() {


    private var themes = mutableListOf<Int>()
    private var selectedItem: Int = 0

    fun setThemesList(list: List<Int>, selected: Int = 0){
        themes = list.toMutableList()
        selectedItem = selected
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeViewHolder {
        return ThemeViewHolder(parent.context, themes[viewType])
    }

    override fun onBindViewHolder(holder: ThemeViewHolder, position: Int) {
        holder.bind(themes[position])
    }

    override fun getItemCount(): Int = themes.size

    override fun getItemViewType(position: Int): Int = position

    inner class ThemeViewHolder(context: Context, theme: Int) :
        RecyclerView.ViewHolder(
            ItemThemeBinding.inflate(
                LayoutInflater.from(
                    ContextThemeWrapper(
                        context,
                        theme
                    )
                ),
                null,
                false
            ).root
        ) {
        private val binding = ItemThemeBinding.bind(itemView)

        fun bind(theme: Int) {
            if (theme == selectedItem){
                Utils.basicAnimationOn(binding.selected).start()
            }else{
                binding.selected.alpha = 0f
            }

            binding.root.setOnClickListener {
                val previousSelectedItem = selectedItem
                selectedItem = theme
                notifyItemChanged(themes.indexOf(previousSelectedItem))
                Utils.basicAnimationOn(binding.selected).start()
                onThemeSelected(theme)

            }

        }
    }
}