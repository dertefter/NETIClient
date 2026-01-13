package com.dertefter.neticlient.ui.settings.theme_creator

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.databinding.ItemColorSelectBinding

class ColorAdapter(
    private val colors: List<Int>,
    private var selectedColor: Int,
    private val onColorSelected: (Int) -> Unit
) : RecyclerView.Adapter<ColorAdapter.ColorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val binding = ItemColorSelectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ColorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        val color = colors[position]
        holder.bind(color, color == selectedColor)
    }

    override fun getItemCount(): Int = colors.size

    inner class ColorViewHolder(private val binding: ItemColorSelectBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(color: Int, isSelected: Boolean) {
            binding.colorView.setBackgroundColor(color)
            binding.selectedIndicator.visibility = if (isSelected) View.VISIBLE else View.GONE
            
            binding.root.setOnClickListener {
                val previousSelected = selectedColor
                selectedColor = color
                notifyItemChanged(colors.indexOf(previousSelected))
                notifyItemChanged(colors.indexOf(selectedColor))
                onColorSelected(color)
            }
        }
    }
}