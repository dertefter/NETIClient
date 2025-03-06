package com.dertefter.neticlient.ui.schedule.week.day

import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.schedule.Lesson
import com.dertefter.neticlient.data.model.schedule.Time
import com.dertefter.neticlient.ui.person.PersonListRecyclerViewAdapter
import com.dertefter.neticlient.ui.person.PersonListStyle
import com.dertefter.neticlient.utils.Utils
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import pl.hypeapp.noiseview.NoiseView

class TimeRecyclerViewAdapter(
    private var lessonList: List<Lesson> = emptyList(),
    val fragment: DayFragment,
    val timeItem: Time,

    ) : RecyclerView.Adapter<TimeRecyclerViewAdapter.TimeViewHolder>() {
    var isNow: Boolean = false
    fun setData(lessonList: List<Lesson>, isNow: Boolean = false) {
        if (this.lessonList != lessonList || this.isNow != isNow){
            this.lessonList = lessonList
            this.isNow = isNow
            Log.e("lessonList", "setData: $lessonList")
            notifyDataSetChanged()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lesson, parent, false)
        return TimeViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeViewHolder, position: Int) {
        val lessonItem = lessonList[position]
        holder.bind(lessonItem, isNow, fragment, timeItem)
    }

    override fun getItemCount(): Int = lessonList.size

    fun setIsNow(b: Boolean) {
        if (isNow != b){
            isNow = b
            notifyDataSetChanged()
        }

    }

    class TimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.title)
        private val type: TextView = itemView.findViewById(R.id.type)
        private val aud: TextView = itemView.findViewById(R.id.aud)
        val personsRecyclerView: RecyclerView = itemView.findViewById(R.id.personsRecyclerView)


        fun bind(lessonItem: Lesson, isNow: Boolean, fragment: DayFragment, timeItem: Time) {
            val adapter = PersonListRecyclerViewAdapter(fragment = fragment, listStyle = PersonListStyle.AVATARS_ONLY)
            itemView.setOnClickListener {
                fragment.showLessonInfo(lessonItem, timeItem)
            }

            title.text = lessonItem.title
            type.text = lessonItem.type
            aud.text = lessonItem.aud
            Log.e("lessonItem.personIds", lessonItem.personIds.toString())
            personsRecyclerView.adapter = adapter
            personsRecyclerView.layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
            if (!lessonItem.personIds.isNullOrEmpty()){
                adapter.setData(lessonItem.personIds)
            }


            if (aud.text.isEmpty()){
                aud.visibility = View.GONE
            } else {
                aud.visibility = View.VISIBLE
            }
            if (type.text.isNotEmpty()) {
                type.visibility = View.VISIBLE
                when {
                    type.text.contains("Лаб") -> {
                        type.backgroundTintList = ColorStateList.valueOf(
                            MaterialColors.getColor(type, com.google.android.material.R.attr.colorTertiaryContainer)
                        )
                        type.setTextColor(
                            MaterialColors.getColor(type, com.google.android.material.R.attr.colorOnTertiaryContainer)
                        )
                    }
                    type.text.contains("Практика") -> {
                        type.backgroundTintList = ColorStateList.valueOf(
                            MaterialColors.getColor(type, com.google.android.material.R.attr.colorPrimaryContainer)
                        )
                        type.setTextColor(
                            MaterialColors.getColor(type, com.google.android.material.R.attr.colorOnPrimaryContainer)
                        )
                    }
                    else -> {
                        type.backgroundTintList = ColorStateList.valueOf(
                            MaterialColors.getColor(type, com.google.android.material.R.attr.colorSecondaryContainer)
                        )
                        type.setTextColor(
                            MaterialColors.getColor(type, com.google.android.material.R.attr.colorOnSecondaryContainer)
                        )
                    }
                }
            } else {
                type.visibility = View.GONE
            }
            if (isNow){
                (itemView as MaterialCardView).setCardBackgroundColor(MaterialColors.getColor(itemView, com.google.android.material.R.attr.colorPrimary))
                title.setTextColor(MaterialColors.getColor(itemView, com.google.android.material.R.attr.colorOnPrimary))

            } else {
                (itemView as MaterialCardView).setCardBackgroundColor(MaterialColors.getColor(itemView, com.google.android.material.R.attr.colorSurfaceContainer))
                title.setTextColor(MaterialColors.getColor(itemView, com.google.android.material.R.attr.colorOnSurface))
            }
        }
    }
}
