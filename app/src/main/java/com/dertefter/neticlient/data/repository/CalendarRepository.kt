package com.dertefter.neticlient.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dertefter.neticlient.data.model.calendar.CalendarEvent
import com.dertefter.neticlient.data.network.NetworkClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class CalendarRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val networkClient: NetworkClient
) {

    suspend fun updateEventsForMonth(year: String, month: String) {
        val events = networkClient.getEvents(year, month)
        if (events != null){
            Log.e("in_repo", "$year $month")
            Log.e("in_repo", events.toString())
            saveEventsForMonth(year, month, events)
        }
    }

    private suspend fun saveEventsForMonth(year: String, month: String, events: List<CalendarEvent>) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("events_${year}_${month}")] = Gson().toJson(events)
        }
    }

    fun getEventsFlowForMonth(year: String, month: String): Flow<List<CalendarEvent>?> {
        return dataStore.data
            .map { preferences ->
                val json_ = preferences[stringPreferencesKey("events_${year}_${month}")]
                try {
                    json_?.let {
                        val type = object : TypeToken<List<CalendarEvent>>() {}.type
                        Gson().fromJson<List<CalendarEvent>>(it, type)
                    }
                } catch (e: Exception) {
                    null
                }
            }
            .distinctUntilChanged()
    }




}
