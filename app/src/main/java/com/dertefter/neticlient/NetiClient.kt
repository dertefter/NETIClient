package com.dertefter.neticlient

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NetiClient : Application() {

    override fun onCreate() {
        super.onCreate()
    }

}