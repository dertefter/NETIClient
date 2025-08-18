package com.dertefter.neticlient.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dertefter.neticlient.data.model.CurrentTimeObject
import com.dertefter.neticlient.data.model.schedule.Lesson
import com.dertefter.neticlient.data.model.schedule.LessonTrigger
import com.dertefter.neticlient.data.model.schedule.Schedule
import com.dertefter.neticlient.data.model.schedule.Time
import com.dertefter.neticlient.data.model.sessia_schedule.SessiaScheduleItem
import com.dertefter.neticlient.data.network.NetworkClient
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.network.model.ResponseType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ScheduleRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val networkClient: NetworkClient
) {

    suspend fun updateSchedule(group: String, isIndividual: Boolean = false) {

        val schedule = networkClient.getSchedule(group, isIndividual)
        if (schedule.responseType == ResponseType.SUCCESS && schedule.data != null) {
            saveSchedule(group, schedule.data as Schedule)
        }
    }

    private suspend fun saveSchedule(group: String, schedule: Schedule) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("schedule_$group")] = Gson().toJson(schedule)
        }
    }

    fun getScheduleFlow(group: String): Flow<Schedule?> {
        return dataStore.data
            .map { preferences ->
                val json_ = preferences[stringPreferencesKey("schedule_$group")]
                try {
                    json_?.let { Gson().fromJson(it, Schedule::class.java) }
                } catch (e: Exception) {
                    null
                }
            }
            .distinctUntilChanged()
    }



    suspend fun updateScheduleSessia(group: String) {
        val schedule = networkClient.getSessiaSchedule(group)
        if (schedule.responseType == ResponseType.SUCCESS && schedule.data != null) {
            saveScheduleSessia(group, schedule.data as  List<SessiaScheduleItem>)
        }
    }

    private suspend fun saveScheduleSessia(group: String, schedule: List<SessiaScheduleItem>) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("schedule_sessia_$group")] = Gson().toJson(schedule)
        }
    }
    fun getScheduleSessiaFlow(group: String): Flow<List<SessiaScheduleItem>?> {
        return dataStore.data
            .map { preferences ->
                val json_ = preferences[stringPreferencesKey("schedule_sessia_$group")]
                try {
                    json_?.let {
                        val type = object : TypeToken<List<SessiaScheduleItem>>() {}.type
                        Gson().fromJson<List<SessiaScheduleItem>>(it, type)
                    }
                } catch (e: Exception) {
                    null
                }
            }
            .distinctUntilChanged()
    }


    suspend fun updateWeekNumber() {
        val currentWeekNumber = networkClient.getCurrentWeekNumber()
        if (currentWeekNumber != null){
            saveWeekNumber(currentWeekNumber)
        }
    }

    private suspend fun saveWeekNumber(weekNumber: Int) {
        dataStore.edit { preferences ->
            preferences[intPreferencesKey("weekNumberInt")] = weekNumber
        }
    }

    fun getWeekNumberFlow(): Flow<Int?> {
        return dataStore.data.map { preferences ->
            preferences[intPreferencesKey("weekNumberInt")]
        }
    }

    fun getWeekLabelFlow(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[stringPreferencesKey("weekLabel")]
        }
    }

    suspend fun updateWeekLabel() {
        val weekLabel = networkClient.getHeaderLabel()
        if (weekLabel != null){
            saveWeekLabel(weekLabel)
        }
    }

    private suspend fun saveWeekLabel(weekLabel: String) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("weekLabel")] = weekLabel
        }
    }


}