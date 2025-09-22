package com.dertefter.neticlient.widgets

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import com.dertefter.neticore.NETICore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import com.dertefter.neticlient.R
import androidx.core.net.toUri
import com.dertefter.neticore.features.schedule.model.Time
import java.time.temporal.ChronoUnit

@AndroidEntryPoint
class NextLessonsWidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var netiCore: NETICore
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val ACTION_UPDATE_PROGRESS = "com.dertefter.neticlient.action.UPDATE_PROGRESS"
    }


    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }


    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_UPDATE_PROGRESS -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val thisWidget = ComponentName(context, NextLessonsWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
                appWidgetIds.forEach { appWidgetId ->
                    updateAppWidget(context, appWidgetManager, appWidgetId)
                }
            }
        }
    }


    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        scope.launch {
            val views = RemoteViews(context.packageName, R.layout.widget_next_lessons)

            try {
                val group = netiCore.userDetailFeature.currentGroup.first()
                views.setViewVisibility(R.id.group, if (group != null) View.VISIBLE else View.GONE)
                if (group == null) {
                    views.setTextViewText(R.id.group, "")
                } else {
                    views.setTextViewText(R.id.group, group)
                    val schedule = netiCore.scheduleFeature.scheduleForGroup(group).first()

                    val nextDayWithLessons = schedule?.findNextDayWithLessonsAfter(
                        date = LocalDate.now(),
                        time = LocalTime.now()
                    )
                    val date = nextDayWithLessons?.getDate()

                    if (date != null){
                        val (text1, text2) = when (date) {
                            LocalDate.now() -> context.getString(R.string.classses_for) to context.getString(R.string.today)
                            LocalDate.now().plusDays(1) -> context.getString(R.string.classses_for) to context.getString(R.string.tomorrow)
                            else -> {
                                val formatter = DateTimeFormatter.ofPattern("d MMMM")
                                context.getString(R.string.classses_for) to date?.format(formatter)
                            }
                        }
                        views.setTextViewText(R.id.date_tv1, text1)
                        views.setTextViewText(R.id.date_tv2, text2)
                        views.setViewVisibility(R.id.date_tv2, if (!text2.isNullOrEmpty()) View.VISIBLE else View.GONE)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                views.setTextViewText(R.id.widget_group_name, "Ошибка загрузки")
            }

            val intent = Intent(context, NextLessonsWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = toUri(Intent.URI_INTENT_SCHEME).toUri()
            }
            views.setRemoteAdapter(R.id.widget_lesson_list, intent)
            views.setEmptyView(R.id.widget_lesson_list, R.id.widget_empty_view)
            appWidgetManager.updateAppWidget(appWidgetId, views)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_lesson_list)
        }


    }


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