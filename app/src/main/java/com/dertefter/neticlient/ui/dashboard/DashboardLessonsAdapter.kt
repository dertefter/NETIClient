package com.dertefter.neticlient.ui.dashboard

import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.item_decoration.AvatarOverlapItemDecoration
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticlient.data.model.schedule.FutureOrPastOrNow
import com.dertefter.neticlient.data.model.schedule.Lesson
import com.dertefter.neticlient.data.model.schedule.LessonDetail
import com.dertefter.neticlient.data.model.schedule.Time
import com.dertefter.neticlient.ui.person.PersonListRecyclerViewAdapter
import com.dertefter.neticlient.ui.person.PersonListStyle
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors

class DashboardLessonsAdapter(
    private var lessonList: List<Lesson> = emptyList(),
    val fragment: DashboardFragment,
    val timeItem: Time,
    val isLegendary: Boolean = false

    ) : RecyclerView.Adapter<DashboardLessonsAdapter.TimeViewHolder>() {
    var futureOrPastOrNow: FutureOrPastOrNow = FutureOrPastOrNow.FUTURE

    fun setData(lessonList: List<Lesson>, futureOrPastOrNow: FutureOrPastOrNow = FutureOrPastOrNow.FUTURE) {
        if (this.lessonList != lessonList || this.futureOrPastOrNow != futureOrPastOrNow){
            this.lessonList = lessonList
            this.futureOrPastOrNow = futureOrPastOrNow
            Log.e("lessonList", "setData: $lessonList")
            notifyDataSetChanged()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeViewHolder {
        val layoutRes = if (!isLegendary) R.layout.item_lesson else R.layout.item_lesson_legendary
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return TimeViewHolder(view, isLegendary)
    }

    override fun onBindViewHolder(holder: TimeViewHolder, position: Int) {
        val lessonItem = lessonList[position]
        holder.bind(lessonItem, futureOrPastOrNow, fragment, timeItem)
    }

    override fun getItemCount(): Int = lessonList.size

    fun updateFutureOrPastOrNow(b: FutureOrPastOrNow) {
        if (futureOrPastOrNow != b){
            this.futureOrPastOrNow = b
            notifyDataSetChanged()
        }

    }

    class TimeViewHolder(itemView: View, isLegendary: Boolean) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.title)
        private val type: TextView = itemView.findViewById(R.id.type)
        private val aud: TextView = itemView.findViewById(R.id.aud)
        private val legendary_indicator: ImageView? = itemView.findViewById(R.id.legendary_indicator)
        private val time: TextView? = itemView.findViewById(R.id.time)


        val personsRecyclerView: RecyclerView = itemView.findViewById(R.id.personsRecyclerView)


        fun bind(lessonItem: Lesson, futureOrPastOrNow: FutureOrPastOrNow, fragment: DashboardFragment, timeItem: Time) {
            val adapter = if (time == null) {
                PersonListRecyclerViewAdapter(fragment = fragment, listStyle = PersonListStyle.AVATARS_ONLY){}
            } else {
                PersonListRecyclerViewAdapter(fragment = fragment, listStyle = PersonListStyle.SMALL_TEXT){}
            }
            itemView.setOnClickListener {
                val lessonDetail = LessonDetail(
                    lessonItem,
                    timeItem,
                    futureOrPastOrNow

                )
                //fragment.showLessonInfo(lessonDetail) TODO
            }


            title.text = lessonItem.title
            type.text = lessonItem.type
            aud.text = lessonItem.aud

            (itemView as MaterialCardView).setStrokeColor(Color.TRANSPARENT)

            time?.text = "${timeItem.timeStart}-${timeItem.timeEnd}"

            personsRecyclerView.adapter = adapter

            if (time != null){
                personsRecyclerView.layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.VERTICAL, false)
            }else {
                personsRecyclerView.layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
                personsRecyclerView.addItemDecoration(AvatarOverlapItemDecoration(itemView.context, R.dimen.margin))
            }


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
                        if (time != null){
                            legendary_indicator?.imageTintList = ColorStateList.valueOf(
                                MaterialColors.getColor(type, com.google.android.material.R.attr.colorTertiary)
                            )
                            time?.setTextColor(
                                MaterialColors.getColor(type, com.google.android.material.R.attr.colorTertiary)
                            )
                            type.setTextColor(
                                MaterialColors.getColor(type, com.google.android.material.R.attr.colorTertiary)
                            )
                        } else {
                            type.backgroundTintList = ColorStateList.valueOf(
                                MaterialColors.getColor(type, com.google.android.material.R.attr.colorTertiaryContainer)
                            )
                            type.setTextColor(
                                MaterialColors.getColor(type, com.google.android.material.R.attr.colorOnTertiaryContainer)
                            )
                        }
                    }
                    type.text.contains("Практика") -> {
                        if (time != null){
                            legendary_indicator?.imageTintList = ColorStateList.valueOf(
                                MaterialColors.getColor(type, com.google.android.material.R.attr.colorPrimary)
                            )
                            time?.setTextColor(
                                MaterialColors.getColor(type, com.google.android.material.R.attr.colorPrimary)
                            )
                            type.setTextColor(
                                MaterialColors.getColor(type, com.google.android.material.R.attr.colorPrimary)
                            )
                        }
                        else {
                            type.backgroundTintList = ColorStateList.valueOf(
                                MaterialColors.getColor(type, com.google.android.material.R.attr.colorPrimaryContainer)
                            )
                            type.setTextColor(
                                MaterialColors.getColor(type, com.google.android.material.R.attr.colorOnPrimaryContainer)
                            )
                        }
                    }
                    else -> {
                        if (time != null){
                            time.setTextColor(
                                MaterialColors.getColor(type, com.google.android.material.R.attr.colorSecondary)
                            )
                            type.setTextColor(
                                MaterialColors.getColor(type, com.google.android.material.R.attr.colorSecondary)
                            )
                            legendary_indicator?.imageTintList = ColorStateList.valueOf(
                                MaterialColors.getColor(type, com.google.android.material.R.attr.colorSecondary)
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
            } else {
                type.visibility = View.GONE
            }
            if (futureOrPastOrNow == FutureOrPastOrNow.NOW && time == null){
                (itemView as MaterialCardView).setStrokeColor(MaterialColors.getColor(itemView, com.google.android.material.R.attr.colorPrimary))
                (itemView as MaterialCardView).setCardBackgroundColor(MaterialColors.getColor(itemView, com.google.android.material.R.attr.colorSurfaceContainer))
                title.setTextColor(MaterialColors.getColor(itemView, com.google.android.material.R.attr.colorOnSurface))

            } else {
                (itemView as MaterialCardView).setCardBackgroundColor(MaterialColors.getColor(itemView, com.google.android.material.R.attr.colorSurfaceContainer))
                title.setTextColor(MaterialColors.getColor(itemView, com.google.android.material.R.attr.colorOnSurface))
            }
        }
    }
}
