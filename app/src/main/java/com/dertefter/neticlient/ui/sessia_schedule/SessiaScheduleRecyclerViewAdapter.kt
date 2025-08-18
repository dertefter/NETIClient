package com.dertefter.neticlient.ui.sessia_schedule

import android.content.res.ColorStateList
import android.text.Spannable
import android.text.SpannableString
import android.text.style.LeadingMarginSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.doOnPreDraw
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.sessia_schedule.SessiaScheduleItem
import com.dertefter.neticlient.ui.person.PersonListRecyclerViewAdapter
import com.dertefter.neticlient.ui.person.PersonListStyle
import com.google.android.material.color.MaterialColors
import java.time.format.DateTimeFormatter
import java.util.Locale

class SessiaScheduleRecyclerViewAdapter(
    private var itemList: List<SessiaScheduleItem> = emptyList(),
    val fragment: SessiaScheduleFragment
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var loading = false
    private val loadingItemCount = 7

    companion object {
        private const val TYPE_ITEM = 0
        private const val TYPE_LOADING = 1
    }

    fun updateList(newList: List<SessiaScheduleItem>) {
        itemList = newList
        notifyDataSetChanged()
    }

    fun setLoading(v: Boolean) {
        if (loading != v) {
            loading = v
            notifyDataSetChanged()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (loading && position >= itemList.size) {
            TYPE_LOADING
        } else {
            TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_ITEM) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_sessia_schedule, parent, false)
            ItemViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_sessia_schedule_loading, parent, false)
            LoadingViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            val item = itemList[position]
            holder.bind(item, fragment)
        }
        // LoadingViewHolder ничего биндить не нужно
    }

    override fun getItemCount(): Int {
        return itemList.size + if (loading) loadingItemCount else 0
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.title)
        private val type: TextView = itemView.findViewById(R.id.type)
        private val aud: TextView = itemView.findViewById(R.id.aud)
        private val time: TextView = itemView.findViewById(R.id.time)
        private val date: TextView = itemView.findViewById(R.id.date)

        private val personsRecyclerView: RecyclerView = itemView.findViewById(R.id.personsRecyclerView)

        fun bind(item: SessiaScheduleItem, fragment: SessiaScheduleFragment) {
            val adapter = PersonListRecyclerViewAdapter(
                fragment = fragment,
                listStyle = PersonListStyle.AVATARS_ONLY
            ) {}

            val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))
            title.text = item.name
            type.text = item.type
            aud.text = item.aud
            time.text = item.time
            date.text = item.getDate().format(formatter)

            personsRecyclerView.adapter = adapter
            personsRecyclerView.layoutManager =
                LinearLayoutManager(itemView.context, LinearLayoutManager.VERTICAL, false)

            if (item.personIds.isNotEmpty()){
                adapter.setData(item.personIds)

                personsRecyclerView.doOnPreDraw {
                    val spannable = SpannableString(title.text)
                    spannable.setSpan(
                        LeadingMarginSpan.Standard(personsRecyclerView.width, 0),
                        0, title.text.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    title.text = spannable

                }


            }



            aud.visibility = if (aud.text.isEmpty()) View.GONE else View.VISIBLE
            type.visibility = if (type.text.isEmpty()) View.GONE else View.VISIBLE

            if (item.type.contains("экз", true)) {
                type.backgroundTintList = ColorStateList.valueOf(
                    MaterialColors.getColor(type, com.google.android.material.R.attr.colorPrimaryContainer)
                )
                type.setTextColor(
                    MaterialColors.getColor(type, com.google.android.material.R.attr.colorOnPrimaryContainer)
                )
            } else {
                type.backgroundTintList = ColorStateList.valueOf(
                    MaterialColors.getColor(type, com.google.android.material.R.attr.colorSecondaryContainer)
                )
                type.setTextColor(
                    MaterialColors.getColor(type, com.google.android.material.R.attr.colorOnSecondaryContainer)
                )
            }
        }
    }

    class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
