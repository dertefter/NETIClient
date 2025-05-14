package com.dertefter.neticlient.widgets.schedule_widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.repository.ScheduleRepository
import com.dertefter.neticlient.data.repository.SettingsRepository
import com.dertefter.neticlient.data.repository.UserRepository
import com.dertefter.neticlient.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import androidx.core.net.toUri
import com.google.android.material.color.MaterialColors
import java.util.Locale


@AndroidEntryPoint
class ScheduleWidget : AppWidgetProvider() {

    @Inject
    lateinit var scheduleRepository: ScheduleRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var userRepository: UserRepository

    companion object {
        const val ACTION_SELECT_DAY = "com.dertefter.neticlient.widgets.schedule_widget.SELECT_DAY"
        val DAY_BUTTONS = listOf(
            R.id.btn_mon,
            R.id.btn_tue,
            R.id.btn_wed,
            R.id.btn_thu,
            R.id.btn_fri,
            R.id.btn_sat
        )
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val prefs = context.getSharedPreferences("schedule_widget_prefs", Context.MODE_PRIVATE)
            val selectedDay = prefs.getInt("selected_day_$appWidgetId", getTodayDayNumber())
            updateAppWidget(context, appWidgetManager, appWidgetId, selectedDay)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_SELECT_DAY) {
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
            val day = intent.getIntExtra("day", 1)
            if (appWidgetId != -1) {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                updateAppWidget(context, appWidgetManager, appWidgetId, day)
            }
        }
    }

    fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        selectedDay: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.schedule_widget)

        for (i in 1..6) {
            val intent = Intent(context, ScheduleWidget::class.java).apply {
                action = ACTION_SELECT_DAY
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                putExtra("day", i)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, appWidgetId * 10 + i, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val btnId = DAY_BUTTONS[i - 1]
            views.setOnClickPendingIntent(btnId, pendingIntent)
            if (i == selectedDay) {
               views.setInt(btnId, "setBackgroundResource", R.drawable.rounded_background)
            } else {
                views.setInt(btnId, "setBackgroundResource", R.drawable.transparent)
            }
        }

        val dayNames = listOf("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота")
        views.setTextViewText(R.id.day_name, dayNames.getOrNull(selectedDay - 1) ?: "")

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        views.setOnClickPendingIntent(R.id.root_layout, pendingIntent)

        views.setTextViewText(R.id.week_label_text_view, context.getString(R.string.loading))
        appWidgetManager.updateAppWidget(appWidgetId, views)

        CoroutineScope(Dispatchers.IO).launch {
            val selectedGroup = userRepository.getSelectedGroupFlow().first()
            if (selectedGroup.isNullOrEmpty()) {
                views.setTextViewText(R.id.week_label_text_view, context.getString(R.string.add_group))
                appWidgetManager.updateAppWidget(appWidgetId, views)
            } else {
                val weekNumber = scheduleRepository.getWeekNumberFlow().first()
                val schedule = scheduleRepository.getScheduleFlow(selectedGroup).first()
                if (schedule != null && weekNumber != null) {
                    val intent = Intent(context, LessonWidgetService::class.java).apply {
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        putExtra("group", selectedGroup)
                        putExtra("weekNumber", weekNumber)
                        putExtra("dayNumber", selectedDay)
                        data = toUri(Intent.URI_INTENT_SCHEME).toUri()
                    }
                    views.setTextViewText(R.id.title_tv, "$selectedGroup • $weekNumber неделя")
                    views.setRemoteAdapter(R.id.list_view, intent)
                    views.setEmptyView(R.id.list_view, R.id.week_label_text_view)
                    views.setTextViewText(R.id.week_label_text_view, context.getString(R.string.no_lessons))
                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.list_view)
                } else {
                    views.setTextViewText(R.id.week_label_text_view, context.getString(R.string.no_lessons))
                }
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    private fun getTodayDayNumber(): Int {
        val cal = Calendar.getInstance()
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            else -> 1
        }
    }

    override fun onEnabled(context: Context) {
    }

    override fun onDisabled(context: Context) {
    }
}
