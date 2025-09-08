package com.dertefter.neticlient

import android.app.Application
import com.dertefter.neticore.NETICore
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class NetiClient : Application() {

    override fun onCreate() {
        super.onCreate()
        NETICore.setupWith(this)
    }

}