package com.dertefter.neticlient.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dertefter.neticlient.data.model.schedule.Lesson
import com.dertefter.neticlient.data.model.schedule.LessonTrigger
import com.dertefter.neticlient.data.model.schedule.Schedule
import com.dertefter.neticlient.data.model.schedule.Time
import com.dertefter.neticlient.data.network.NetworkClient
import com.dertefter.neticlient.data.network.model.ResponseType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ScheduleRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val networkClient: NetworkClient
) {

    suspend fun fetchSchedule(group: String, isIndividual: Boolean = false): Schedule? {
        val schedule = networkClient.getSchedule(group, isIndividual)
        if (schedule.responseType == ResponseType.SUCCESS && schedule.data != null) {
            saveSchedule(group, schedule.data as Schedule)
        }
        return schedule.data as Schedule?
    }

    private suspend fun saveSchedule(group: String, schedule: Schedule) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("schedule_$group")] = Gson().toJson(schedule)
        }
    }

    suspend fun getLocalSchedule(group: String): Flow<Schedule?> {
        return dataStore.data.map { preferences ->
            val json_ = preferences[stringPreferencesKey("schedule_$group")]
            if (json_ != null) {
                Gson().fromJson(json_, Schedule::class.java)
            } else {
                null
            }
        }
    }


    suspend fun fetchWeekNumberList(group: String): List<Int>? {
        val response = networkClient.getWeekNumberList(group)
        if (response.responseType == ResponseType.SUCCESS && !(response.data as List<Int>?).isNullOrEmpty()) {
            saveWeekNumberList(group, response.data as List<Int>)
        }
        return (response.data as List<Int>?)
    }

    private suspend fun saveWeekNumberList(group: String, list: List<Int>) {
        dataStore.edit { preferences ->
            Log.e("saveWeekNumberList", list.toString())
            preferences[stringPreferencesKey("week_list_$group")] = Gson().toJson(list)
        }
    }

    suspend fun getLocalWeekNumberList(group: String): Flow<List<Int>?> {
        return dataStore.data.map { preferences ->
            val json_ = preferences[stringPreferencesKey("week_list_$group")]
            if (json_ != null) {
                val type = object : TypeToken<List<Int>>() {}.type
                Gson().fromJson<List<Int>>(json_, type)
            } else {
                null
            }
        }
    }

    suspend fun fetchCurrentWeekNumber(): Int? {
        val currentWeekNumber = networkClient.getCurrentWeekNumber()
        if (currentWeekNumber != null){
            saveWeekNumber(currentWeekNumber)
            return currentWeekNumber
        } else {
            val currentWeekNumberLocal = getLocalWeekNumberList().first()
            if (currentWeekNumberLocal != null){
                return currentWeekNumberLocal
            } else {
                return null
            }
        }
    }

    private suspend fun saveWeekNumber(weekNumber: Int) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("weekNumber")] = Gson().toJson(weekNumber)
        }
    }

    suspend fun getLocalWeekNumberList(): Flow<Int?> {
        return dataStore.data.map { preferences ->
            val json_ = preferences[stringPreferencesKey("weekNumber")]
            if (json_ != null) {
                Gson().fromJson(json_, Int::class.java)
            } else {
                null
            }
        }
    }


}