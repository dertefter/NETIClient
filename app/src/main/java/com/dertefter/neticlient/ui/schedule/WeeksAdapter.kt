package com.dertefter.neticlient.ui.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.R
import com.dertefter.neticore.features.schedule.model.Week
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors

class WeeksAdapter(
    private var weeks: List<Week> = emptyList(),
    private val onItemClick: (Week) -> Unit
) : RecyclerView.Adapter<WeeksAdapter.WeekViewHolder>() {

    var currentWeekNumber: Int? = null

    fun updateWeeks(weeks: List<Week>){
        this.weeks = weeks
        notifyDataSetChanged()
    }

    fun updateCurrentWeekNumber(v: Int){
        currentWeekNumber = v
        notifyDataSetChanged()
    }

    inner class WeekViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.weekNumber)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeekViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_week, parent, false)
        return WeekViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeekViewHolder, position: Int) {
        val week = weeks[position]
        holder.textView.text = week.weekNumber.toString()
        holder.itemView.setOnClickListener { onItemClick(week) }
        if (week.weekNumber == currentWeekNumber){
            val radius = holder.itemView.context.resources.getDimensionPixelSize(R.dimen.d6)
            (holder.itemView as MaterialCardView).setRadius(radius.toFloat())
            holder.textView.setTextColor(MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorOnTertiary))
            (holder.itemView as MaterialCardView).setCardBackgroundColor(MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorTertiary))
        } else {
            val radius = holder.itemView.context.resources.getDimensionPixelSize(R.dimen.d3)
            (holder.itemView as MaterialCardView).setRadius(radius.toFloat())
            holder.textView.setTextColor(MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorOnTertiaryContainer))
            (holder.itemView as MaterialCardView).setCardBackgroundColor(MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorTertiaryContainer))
        }
    }

    override fun getItemCount() = weeks.size
}