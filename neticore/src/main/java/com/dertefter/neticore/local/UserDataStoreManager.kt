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
import java.io.File

@SuppressLint("StaticFieldLeak")
class UserDataStoreManager(private val context: Context) {

    private val stores = java.util.concurrent.ConcurrentHashMap<String, DataStore<Preferences>>()

    private val _currentStore = MutableStateFlow(getStore("guest"))
    val currentStore: StateFlow<DataStore<Preferences>> = _currentStore.asStateFlow()

    private val _currentName = MutableStateFlow("guest")
    val currentName: StateFlow<String> = _currentName.asStateFlow()

    fun switchToUser(userLogin: String) {
        val name = userLogin
        _currentStore.value = getStore(name)
        _currentName.value = name
    }

    fun resetToGuest() {
        val name = "guest"
        _currentStore.value = getStore(name)
        _currentName.value = name
    }

    fun getDataStoreName(): String = _currentName.value

    private fun getStore(name: String): DataStore<Preferences> =
        stores.computeIfAbsent(name) { key ->
            PreferenceDataStoreFactory.create(
                produceFile = { context.preferencesDataStoreFile(key) }
            )
        }

    private fun Context.preferencesDataStoreFile(name: String): File =
        this.filesDir.resolve("datastore/$name.preferences_pb")
}

