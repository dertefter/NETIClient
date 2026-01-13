package com.dertefter.neticlient.ui.schedule

import android.content.res.ColorStateList
import android.text.Spannable
import android.text.SpannableString
import android.text.style.LeadingMarginSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.core.view.isGone
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.item_decoration.AvatarOverlapItemDecoration
import com.dertefter.neticlient.data.model.CurrentTimeObject
import com.dertefter.neticlient.databinding.ItemLessonBinding
import com.dertefter.neticlient.ui.person.AvatarListAdapter
import com.dertefter.neticore.features.person_detail.PersonDetailFeature
import com.dertefter.neticore.features.schedule.model.Lesson
import com.google.android.material.color.MaterialColors
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class LessonsAdapter(
    private var lessons: List<Lesson>,
    val lifecycleOwner: LifecycleOwner,
    val corners: Int = 1, //0 - только верх, 1 - оба, 2 - низочек
    private val personDetailFeature: PersonDetailFeature,
    private val onLessonClick: (Lesson) -> Unit
) : RecyclerView.Adapter<LessonsAdapter.LessonViewHolder>() {

    inner class LessonViewHolder(val binding: ItemLessonBinding) :
        RecyclerView.ViewHolder(binding.root) {


        val cardColorFuture = MaterialColors.getColor(
            itemView,
            com.google.android.material.R.attr.colorSurfaceContainer
        )

        val cardColorNow = MaterialColors.getColor(
            itemView,
            com.google.android.material.R.attr.colorSurfaceContainerHigh
        )

        val cardColorPast = MaterialColors.getColor(
            itemView,
            com.google.android.material.R.attr.colorSurfaceContainer
        )

        val alphaNow = 1f
        val alphaPast = 0.5f
        val alphaFuture = 1f


        fun getTypeColors(type: String = ""): Pair<Int, Int> {
            val bgColor: Int = when {
                type.contains("лекц", ignoreCase = true) -> {
                    MaterialColors.getColor(
                        itemView,
                        com.google.android.material.R.attr.colorTertiaryContainer
                    )
                }
                type.contains("прак", ignoreCase = true) -> {
                    MaterialColors.getColor(
                        itemView,
                        com.google.android.material.R.attr.colorPrimaryContainer
                    )
                }
                type.contains("лаб", ignoreCase = true) -> {
                    MaterialColors.getColor(
                        itemView,
                        com.google.android.material.R.attr.colorSecondaryContainer
                    )
                }
                else -> {
                    MaterialColors.getColor(
                        itemView,
                        com.google.android.material.R.attr.colorSurfaceContainerHigh
                    )
                }
            }

            val textColor: Int = when {
                type.contains("лекц", ignoreCase = true) -> {
                    MaterialColors.getColor(
                        itemView,
                        com.google.android.material.R.attr.colorOnTertiaryContainer
                    )
                }
                type.contains("прак", ignoreCase = true) -> {
                    MaterialColors.getColor(
                        itemView,
                        com.google.android.material.R.attr.colorOnPrimaryContainer
                    )
                }
                type.contains("лаб", ignoreCase = true) -> {
                    MaterialColors.getColor(
                        itemView,
                        com.google.android.material.R.attr.colorOnSecondaryContainer
                    )
                }
                else -> {
                    MaterialColors.getColor(
                        itemView,
                        com.google.android.material.R.attr.colorOnSurface
                    )
                }
            }

            return bgColor to textColor
        }


        private var timeUpdateJob: Job? = null

        fun bind(lesson: Lesson) {

            val radiusMax = binding.root.resources.getDimension(R.dimen.default_list_outer_radius)
            val radiusMin = binding.root.resources.getDimension(R.dimen.default_list_innier_radius)

            val shapeModel = when (corners) {
                0 -> ShapeAppearanceModel()
                    .toBuilder()
                    .setTopLeftCorner(CornerFamily.ROUNDED, radiusMin)
                    .setTopRightCorner(CornerFamily.ROUNDED, radiusMax)
                    .setBottomLeftCorner(CornerFamily.ROUNDED, radiusMin)
                    .setBottomRightCorner(CornerFamily.ROUNDED, radiusMin)
                    .build()

                2 -> ShapeAppearanceModel()
                    .toBuilder()
                    .setTopLeftCorner(CornerFamily.ROUNDED, radiusMin)
                    .setTopRightCorner(CornerFamily.ROUNDED, radiusMin)
                    .setBottomLeftCorner(CornerFamily.ROUNDED, radiusMin)
                    .setBottomRightCorner(CornerFamily.ROUNDED, radiusMax)
                    .build()

                else -> ShapeAppearanceModel()
                    .toBuilder()
                    .setAllCorners(CornerFamily.ROUNDED, radiusMin)
                    .build()
            }

            binding.root.shapeAppearanceModel = shapeModel


            binding.title.text = lesson.title
            binding.type.text = lesson.type
            binding.aud.text = lesson.aud

            binding.type.isGone = lesson.type.isEmpty()
            binding.aud.isGone = lesson.aud.isEmpty()

            binding.personsRecyclerView.layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
            val avatarListAdapter = AvatarListAdapter(lesson.personIds, lifecycleOwner, personDetailFeature)
            binding.personsRecyclerView.adapter = avatarListAdapter

            binding.personsRecyclerView.doOnPreDraw {
                val spannable = SpannableString(binding.title.text)
                spannable.setSpan(
                    LeadingMarginSpan.Standard( binding.personsRecyclerView.width, 0),
                    0, binding.title.text.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                binding.title.text = spannable
                binding.titleNow.text = spannable

            }

            if (binding.personsRecyclerView.itemDecorationCount == 0){
                binding.personsRecyclerView.addItemDecoration(
                    AvatarOverlapItemDecoration(
                        itemView.context, R.dimen.d4
                    )
                )
            }

            binding.root.setOnClickListener {
                onLessonClick(lesson)
            }

            subscribeToTimeUpdates(lesson)
        }

        private fun subscribeToTimeUpdates(lesson: Lesson) {
            timeUpdateJob?.cancel()

            timeUpdateJob = lifecycleOwner.lifecycleScope.launch {
                combine(
                    CurrentTimeObject.currentTimeFlow,
                    CurrentTimeObject.currentDateFlow
                ) { currentTime, currentDate ->
                    Pair(currentTime, currentDate)
                }.collect { (currentTime, currentDate) ->
                    if (currentTime != null && currentDate != null) {
                        updateProgressBasedOnTime(lesson, currentTime, currentDate)
                    }
                }
            }
        }

        private fun updateProgressBasedOnTime(
            lesson: Lesson,
            currentTime: LocalTime,
            currentDate: LocalDate
        ) {
            try {
                val startTime = LocalTime.parse(lesson.timeStart)
                val endTime = LocalTime.parse(lesson.timeEnd)

                when {
                    currentDate.isBefore(lesson.getLocalDate()) -> onFuture()
                    currentDate.isAfter(lesson.getLocalDate()) -> onPast()
                    currentTime.isBefore(startTime) -> onFuture()
                    currentTime.isAfter(endTime) -> onPast()
                    else -> onNow()
                }
            } catch (e: Exception) {
                onFuture()
                Log.e("LessonsAdapter", e.stackTraceToString())
            }
        }

        private fun onPast() {
            binding.nowIndicator.isGone = true
            binding.titleNow.isGone = true
            binding.title.isGone = false
            binding.root.setCardBackgroundColor(cardColorPast)
            binding.root.alpha = alphaPast
            val typeColors = getTypeColors()
            binding.type.backgroundTintList = ColorStateList.valueOf(typeColors.first)
            binding.type.setTextColor(typeColors.second)
        }

        private fun onFuture() {
            binding.nowIndicator.isGone = true
            binding.titleNow.isGone = true
            binding.title.isGone = false
            binding.root.setCardBackgroundColor(cardColorFuture)
            binding.root.alpha = alphaFuture
            val typeColors = getTypeColors(binding.type.text.toString())
            binding.type.backgroundTintList = ColorStateList.valueOf(typeColors.first)
            binding.type.setTextColor(typeColors.second)

        }

        private fun onNow() {
            binding.nowIndicator.isGone = false
            binding.titleNow.isGone = false
            binding.title.isGone = true
            binding.root.setCardBackgroundColor(cardColorNow)
            binding.root.alpha = alphaNow
            val typeColors = getTypeColors(binding.type.text.toString())
            binding.type.backgroundTintList = ColorStateList.valueOf(typeColors.first)
            binding.type.setTextColor(typeColors.second)
        }

        fun unbind() {
            timeUpdateJob?.cancel()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        val binding = ItemLessonBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LessonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        holder.bind(lessons[position])
    }

    override fun onViewRecycled(holder: LessonViewHolder) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

    override fun getItemCount(): Int = lessons.size

    fun updateData(newLessons: List<Lesson>) {
        lessons = newLessons
        notifyDataSetChanged()
    }
}
