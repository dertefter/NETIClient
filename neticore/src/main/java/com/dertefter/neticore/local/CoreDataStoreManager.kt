package com.dertefter.neticore.local

import android.annotation.SuppressLint
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CoreDataStoreManager(private val context: Context) {

    val dataStore: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("app") }
        )
    }
}