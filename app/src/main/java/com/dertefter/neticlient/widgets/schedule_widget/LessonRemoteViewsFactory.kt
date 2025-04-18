package com.dertefter.neticlient.widgets.schedule_widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.net.toUri
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.schedule.Lesson
import com.dertefter.neticlient.data.model.schedule.LessonTrigger
import com.dertefter.neticlient.data.repository.ScheduleRepository
import com.dertefter.neticlient.data.repository.SettingsRepository
import com.dertefter.neticlient.data.repository.UserRepository
import com.dertefter.neticlient.di.WidgetDependencies
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject



class LessonRemoteViewsFactory(
    private val context: Context,
    val group: String?,
    val weekNumber: Int,
    val dayNumber: Int
) : RemoteViewsService.RemoteViewsFactory {


    lateinit var scheduleRepository: ScheduleRepository
    lateinit var settingsRepository: SettingsRepository
    lateinit var userRepository: UserRepository


    private var lessons = mutableListOf<Lesson>()

    override fun onCreate() {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetDependencies::class.java
        )
        scheduleRepository = entryPoint.scheduleRepository
        settingsRepository = entryPoint.settingsRepository
        userRepository = entryPoint.userRepository

        loadLessons()
    }

    override fun onDataSetChanged() {
       // lessons.clear()
    }

    override fun onDestroy() {
        lessons.clear()
    }

    override fun getCount(): Int = lessons.size

    override fun getViewAt(position: Int): RemoteViews {
        val lesson = lessons[position]
        val rv = RemoteViews(context.packageName, R.layout.item_lesson_widget)

        rv.setTextViewText(R.id.title, lesson.title)
        rv.setTextViewText(R.id.type, lesson.type)
        rv.setTextViewText(R.id.aud, lesson.aud)
        if (lesson.aud.isNotEmpty()){
            rv.setViewVisibility(R.id.aud, View.VISIBLE)
        }else{
            rv.setViewVisibility(R.id.aud, View.GONE)
        }
        if (lesson.type.isNotEmpty()){
            rv.setViewVisibility(R.id.type, View.VISIBLE)
        }else{
            rv.setViewVisibility(R.id.type, View.GONE)
        }
        rv.setTextViewText(R.id.timeStart, lesson.timeStart)
        rv.setTextViewText(R.id.timeEnd, lesson.timeEnd)
        Log.e("getViewAt:", "$position, $lesson")
        return rv
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = position.toLong()
    override fun hasStableIds(): Boolean = true

    private fun loadLessons() {
        val schedule = group?.let { runBlocking { scheduleRepository.getLocalSchedule(group = it).first() } }
        if (schedule != null) {
            val week = schedule.weeks.find { it.weekNumber == weekNumber }
            val day = week?.days?.find { it.dayNumber == dayNumber }
            val newList = day?.getAllLessons()
            if (newList != null){
                Log.e("newList", newList.toString())
                lessons = newList.toMutableList()
            }
        }
    }

}
