package com.dertefter.neticlient.widgets

import android.content.Intent
import android.widget.RemoteViewsService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NextLessonsWidgetService : RemoteViewsService() {

    @Inject
    lateinit var factory: NextLessonsWidgetRemoteViewsFactory.Factory

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return factory.create(intent)
    }
}