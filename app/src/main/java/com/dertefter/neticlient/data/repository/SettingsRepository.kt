package com.dertefter.neticlient.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dertefter.neticlient.common.Constants
import com.dertefter.neticlient.data.model.User
import com.dertefter.neticlient.data.model.UserInfo
import com.dertefter.neticlient.data.model.messages.Message
import com.dertefter.neticlient.data.network.NetworkClient
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.network.model.ResponseType
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    suspend fun setScheduleService(boolean: Boolean) {
        dataStore.edit { pref ->
            pref[booleanPreferencesKey("scheduleService")] = boolean
        }
    }

    suspend fun setLegendaryCards(boolean: Boolean) {
        dataStore.edit { pref ->
            pref[booleanPreferencesKey("legendaryCards")] = boolean
        }
    }

    fun getScheduleService(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[booleanPreferencesKey("scheduleService")] == true
        }
    }

    suspend fun setNotifyFutureLessons(boolean: Boolean) {
        dataStore.edit { pref ->
            pref[booleanPreferencesKey("notifyFutureLessons")] = boolean
        }
    }

    fun getNotifyFutureLessons(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[booleanPreferencesKey("notifyFutureLessons")] ?: false
        }
    }

    suspend fun setMaterialYou(boolean: Boolean) {
        dataStore.edit { pref ->
            pref[booleanPreferencesKey("MaterialYou")] = boolean
        }
    }

    fun getMaterialYou(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[booleanPreferencesKey("MaterialYou")] ?: false
        }
    }

    fun getOnBoarding(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[booleanPreferencesKey("OnBoarding")] ?: true
        }
    }

    suspend fun setOnBoarding(enable: Boolean) {
        dataStore.edit { pref ->
            pref[booleanPreferencesKey("OnBoarding")] = enable
        }
    }

    suspend fun setCurrentLessonNotification(enable: Boolean) {
        dataStore.edit { pref ->
            pref[booleanPreferencesKey("currentLessonNotification")] = enable
        }
    }

    fun getCurrentLessonNotification(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[booleanPreferencesKey("currentLessonNotification")] ?: false
        }
    }

    suspend fun setVerticalSchedule(enable: Boolean) {
        dataStore.edit { pref ->
            pref[booleanPreferencesKey("verticalSchedule")] = enable
        }
    }

    fun getVerticalSchedule(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[booleanPreferencesKey("verticalSchedule")] ?: false
        }
    }

    suspend fun setCacheMessages(enable: Boolean) {
        dataStore.edit { pref ->
            pref[booleanPreferencesKey("cacheMessages")] = enable
        }
    }

    fun getCacheMessages(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[booleanPreferencesKey("cacheMessages")] ?: true
        }
    }

    fun getLegendaryCards(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[booleanPreferencesKey("legendaryCards")] ?: false
        }
    }

    fun getDashboardTitle(): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[stringPreferencesKey("dashboardTitle")] ?: ""
        }
    }

    suspend fun setDashboardTitle(state: String) {
        dataStore.edit { pref ->
            pref[stringPreferencesKey("dashboardTitle")] = state
        }
    }

    fun getNotifyValue(): Flow<Int> {
        return dataStore.data.map { preferences ->
            preferences[intPreferencesKey("notifyValue")] ?: 15
        }
    }

    suspend fun setNotifyValue(state: Int) {
        dataStore.edit { pref ->
            pref[intPreferencesKey("notifyValue")] = state
        }
    }

}