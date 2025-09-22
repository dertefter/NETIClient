package com.dertefter.neticlient.widgets

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.dertefter.neticlient.R
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.features.schedule.model.Lesson
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

class NextLessonsWidgetRemoteViewsFactory @AssistedInject constructor(
    @ApplicationContext private val context: Context,
    private val netiCore: NETICore,
    @Assisted private val intent: Intent
) : RemoteViewsService.RemoteViewsFactory {

    @AssistedFactory
    interface Factory {
        fun create(intent: Intent): NextLessonsWidgetRemoteViewsFactory
    }

    private var lessons: List<Lesson> = emptyList()

    override fun onCreate() {
    }

    override fun onDataSetChanged() {
        runBlocking {
            try {
                val group = netiCore.userDetailFeature.currentGroup.first() ?: return@runBlocking
                val schedule = netiCore.scheduleFeature.scheduleForGroup(group).first()

                val nextDayWithLessons = schedule?.findNextDayWithLessonsAfter(
                    date = LocalDate.now(),
                    time = LocalTime.now()
                )
                lessons = nextDayWithLessons?.getAllLessons() ?: emptyList()
            } catch (e: Exception) {
                lessons = emptyList()
            }
        }
    }

    override fun onDestroy() {
        lessons = emptyList()
    }

    override fun getCount(): Int = lessons.size

    override fun getViewAt(position: Int): RemoteViews {
        val lesson = lessons[position]
        val views = RemoteViews(context.packageName, R.layout.widget_next_lessons_item)

        views.setTextViewText(R.id.timeStart, lesson.timeStart)
        views.setTextViewText(R.id.timeEnd, lesson.timeEnd)
        views.setTextViewText(R.id.title, lesson.title)
        views.setTextViewText(R.id.aud, lesson.aud)
        views.setTextViewText(R.id.type, lesson.type)
        views.setViewVisibility(R.id.aud, if (lesson.aud.isNotEmpty()) View.VISIBLE else View.GONE)
        views.setViewVisibility(R.id.type, if (lesson.type.isNotEmpty()) View.VISIBLE else View.GONE)
        return views
    }


    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true


    private fun calculateProgress(
        startTime: LocalTime?,
        endTime: LocalTime?,
        currentTime: LocalTime?
    ): Int {
        if (startTime == null || endTime == null || currentTime == null) return 0

        val totalMinutes = startTime.until(endTime, ChronoUnit.MINUTES)
        val elapsedMinutes = startTime.until(currentTime, ChronoUnit.MINUTES)

        return if (totalMinutes > 0) {
            ((elapsedMinutes.toDouble() / totalMinutes.toDouble()) * 100)
                .toInt().coerceIn(0, 100)
        } else 0
    }

}