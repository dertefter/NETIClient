package com.dertefter.neticlient.widgets.schedule_widget

import android.content.Intent
import android.widget.RemoteViewsService

class LessonWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        val group = intent.getStringExtra("group")
        val weekNumber = intent.getIntExtra("weekNumber", 0)
        val dayNumber = intent.getIntExtra("dayNumber", 0)

        return LessonRemoteViewsFactory(applicationContext, group, weekNumber, dayNumber)
    }
}