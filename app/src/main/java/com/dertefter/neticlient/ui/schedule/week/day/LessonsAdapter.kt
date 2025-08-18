package com.dertefter.neticlient.ui.schedule.week.day

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.LeadingMarginSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.item_decoration.AvatarOverlapItemDecoration
import com.dertefter.neticlient.data.model.schedule.FutureOrPastOrNow
import com.dertefter.neticlient.data.model.schedule.Lesson
import com.dertefter.neticlient.data.model.schedule.LessonDetail
import com.dertefter.neticlient.data.model.schedule.Time
import com.dertefter.neticlient.ui.person.PersonListRecyclerViewAdapter
import com.dertefter.neticlient.ui.person.PersonListStyle
import com.dertefter.neticlient.ui.schedule.lesson_view.LessonViewBottomSheetFragment
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors

class LessonsAdapter(
    private var lessonList: List<Lesson> = emptyList(),
    val fragment: Fragment,
    val timeItem: Time

    ) : RecyclerView.Adapter<LessonsAdapter.TimeViewHolder>() {
    var futureOrPastOrNow: FutureOrPastOrNow = FutureOrPastOrNow.FUTURE

    fun setData(lessonList: List<Lesson>, futureOrPastOrNow: FutureOrPastOrNow = FutureOrPastOrNow.FUTURE) {
        if (this.lessonList != lessonList || this.futureOrPastOrNow != futureOrPastOrNow){
            this.lessonList = lessonList
            this.futureOrPastOrNow = futureOrPastOrNow
            
            notifyDataSetChanged()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeViewHolder {
        val layoutRes = R.layout.item_lesson
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return TimeViewHolder(view)
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

    class TimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.title)
        private val type: TextView = itemView.findViewById(R.id.type)
        private val type_exp: TextView = itemView.findViewById(R.id.type_exp)
        private val aud: TextView = itemView.findViewById(R.id.aud)


        val personsRecyclerView: RecyclerView = itemView.findViewById(R.id.personsRecyclerView)


        fun bind(lessonItem: Lesson, futureOrPastOrNow: FutureOrPastOrNow, fragment: Fragment, timeItem: Time) {
            val adapter = PersonListRecyclerViewAdapter(fragment = fragment, listStyle = PersonListStyle.AVATARS_ONLY){}

            itemView.setOnClickListener {
                val lessonDetail = LessonDetail(
                    lessonItem,
                    timeItem,
                    futureOrPastOrNow

                )
                val bottomSheet = LessonViewBottomSheetFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable("lessonDetail", lessonDetail)
                    }
                }
                bottomSheet.show(fragment.parentFragmentManager, "LessonDetail")
            }


            title.text = lessonItem.title
            type.text = lessonItem.type
            type_exp.text = lessonItem.type
            aud.text = lessonItem.aud

            (itemView as MaterialCardView).setStrokeColor(Color.TRANSPARENT)

            personsRecyclerView.adapter = adapter

            personsRecyclerView.layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
            personsRecyclerView.addItemDecoration(AvatarOverlapItemDecoration(itemView.context, R.dimen.margin))


            if (lessonItem.personIds.isNotEmpty()){
                adapter.setData(lessonItem.personIds)

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
                            MaterialColors.getColor(type, com.google.android.material.R.attr.colorSecondaryContainer)
                        )
                        type.setTextColor(
                            MaterialColors.getColor(type, com.google.android.material.R.attr.colorOnSecondaryContainer)
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
                            MaterialColors.getColor(type, com.google.android.material.R.attr.colorTertiaryContainer)
                        )
                        type.setTextColor(
                            MaterialColors.getColor(type, com.google.android.material.R.attr.colorOnTertiaryContainer)
                        )


                    }
                }
            } else {
                type.visibility = View.GONE
            }
            if (futureOrPastOrNow == FutureOrPastOrNow.NOW){
                (itemView as MaterialCardView).strokeColor = MaterialColors.getColor(itemView, com.google.android.material.R.attr.colorPrimaryVariant)
                (itemView as MaterialCardView).setCardBackgroundColor(MaterialColors.getColor(itemView, com.google.android.material.R.attr.colorSurfaceContainer))
                title.setTextColor(MaterialColors.getColor(itemView, com.google.android.material.R.attr.colorOnSurface))
            }

            if (futureOrPastOrNow == FutureOrPastOrNow.PAST){
                (itemView as MaterialCardView).alpha = 0.6f
                (itemView as MaterialCardView).setCardBackgroundColor(MaterialColors.getColor(itemView, com.google.android.material.R.attr.colorSurfaceContainerHighest))
                title.setTextColor(MaterialColors.getColor(itemView, com.google.android.material.R.attr.colorOnSurfaceVariant))
                type.backgroundTintList = ColorStateList.valueOf(
                    MaterialColors.getColor(type, com.google.android.material.R.attr.colorSurfaceContainerHigh)
                )
                type.setTextColor(
                    MaterialColors.getColor(type, com.google.android.material.R.attr.colorOnSurfaceVariant)
                )
                aud.backgroundTintList = ColorStateList.valueOf(
                    MaterialColors.getColor(type, com.google.android.material.R.attr.colorSurfaceContainerHigh)
                )
                aud.setTextColor(
                    MaterialColors.getColor(type, com.google.android.material.R.attr.colorOnSurfaceVariant)
                )


            }
        }
    }
}
