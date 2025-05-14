package com.dertefter.neticlient.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
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
import javax.inject.Inject


@AndroidEntryPoint
class CurrentWeekLabelWidget : AppWidgetProvider() {

    @Inject
    lateinit var scheduleRepository: ScheduleRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var userRepository: UserRepository

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
    }

    override fun onDisabled(context: Context) {
    }

    fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        var widgetText = context.getString(R.string.loading)
        val views = RemoteViews(context.packageName, R.layout.current_week_label_widget)
        views.setTextViewText(R.id.week_label_text_view, widgetText)

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        views.setOnClickPendingIntent(R.id.root_layout, pendingIntent)


        CoroutineScope(Dispatchers.IO).launch {
            val weekLabel = scheduleRepository.getWeekLabelFlow().first()
            if (weekLabel != null){
                widgetText = weekLabel
                views.setTextViewText(R.id.week_label_text_view, widgetText)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}

