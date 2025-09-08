package com.dertefter.neticore.features.schedule

import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dertefter.neticore.features.schedule.model.Schedule
import com.dertefter.neticore.local.UserDataStoreManager
import com.dertefter.neticore.network.NetworkClient
import com.dertefter.neticore.network.ResponseType
import com.dertefter.neticore.network.methods.BaseMethods
import com.dertefter.neticore.network.methods.CiuMethods
import com.google.gson.Gson
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class ScheduleFeature(
    val client: NetworkClient,
    val userDataStoreManager: UserDataStoreManager
) {

    private val ciuMethods = CiuMethods(client.ciuApiService)
    private val baseMethods = BaseMethods(client.baseApiService)
    private val gson = Gson()

    val status = MutableStateFlow(ResponseType.LOADING)

    val WEEK_NUMBER_KEY = intPreferencesKey("week_number")
    val WEEK_LABEL_KEY = stringPreferencesKey("week_label")


    @OptIn(ExperimentalCoroutinesApi::class)
    val weekNumber: Flow<Int?> = userDataStoreManager.currentStore
        .flatMapLatest { dataStore ->
            dataStore.data.map { prefs ->
                prefs[WEEK_NUMBER_KEY]
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    val weekLabel: Flow<String?> = userDataStoreManager.currentStore
        .flatMapLatest { dataStore ->
            dataStore.data.map { prefs ->
                prefs[WEEK_LABEL_KEY]
            }
        }


    suspend fun updateWeekLabel() {
        val weekLabel = baseMethods.fetchWeekLabel()
        if (!weekLabel.isNullOrEmpty()) {
            userDataStoreManager.currentStore.value.edit { prefs ->
                prefs[WEEK_LABEL_KEY] = weekLabel
            }
        }
    }

    suspend fun updateWeekNumber() {
        val weekNumber = baseMethods.fetchWeekNumber()
        if (weekNumber != null) {
            userDataStoreManager.currentStore.value.edit { prefs ->
                prefs[WEEK_NUMBER_KEY] = weekNumber
            }
        }
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    fun scheduleForGroup(symGroup: String): Flow<Schedule?> {
        val schedule: Flow<Schedule?> = userDataStoreManager.currentStore
            .flatMapLatest { dataStore ->
                dataStore.data.map { prefs ->
                    prefs[stringPreferencesKey("schedule_group_$symGroup")]?.let { json ->
                        try {
                            gson.fromJson(json, Schedule::class.java)
                        } catch (e: Exception) {
                            null
                        }
                    }
                }
            }
        return schedule
    }



    suspend fun updateScheduleForGroup(symGroup: String){


        if (scheduleForGroup(symGroup).first() == null){
            status.value = ResponseType.LOADING
        } else {
            status.value = ResponseType.SUCCESS
        }

        val weekNumberListAndFirstDate = baseMethods.getWeekNumberListAndFirstDayDate(symGroup)

        if (weekNumberListAndFirstDate == null){
            status.value = ResponseType.ERROR
        }else{
            val individualSchedule = fetchScheduleForIndividual(symGroup, weekNumberListAndFirstDate)
            Log.e("updateScheduleForGroup individualSchedule", individualSchedule.toString())
            if (individualSchedule != null){
                saveScheduleForGroup(symGroup, individualSchedule)
                status.value = ResponseType.SUCCESS
            } else {
                val guestSchedule = fetchScheduleForGuest(symGroup, weekNumberListAndFirstDate)
                Log.e("updateScheduleForGroup guestSchedule", guestSchedule.toString())
                if (guestSchedule != null){
                    saveScheduleForGroup(symGroup, guestSchedule)
                    status.value = ResponseType.SUCCESS
                } else {
                    status.value = ResponseType.ERROR
                }
            }

        }
    }

    private suspend fun saveScheduleForGroup(symGroup: String, schedule: Schedule) {
        val scheduleKey = stringPreferencesKey("schedule_group_$symGroup")
        val scheduleJson = gson.toJson(schedule)
        userDataStoreManager.currentStore.first().edit { preferences ->
            preferences[scheduleKey] = scheduleJson
        }
    }

    suspend fun fetchScheduleForGuest(symGroup: String, weekNumberListAndFirstDate: Pair<String, List<Int>>): Schedule? {
        val schedule: Schedule? = baseMethods.fetchScheduleForGuest(symGroup, weekNumberListAndFirstDate)
        return schedule
    }

    suspend fun fetchScheduleForIndividual(symGroup: String, weekNumberListAndFirstDate: Pair<String, List<Int>>): Schedule? {
        val schedule: Schedule? = ciuMethods.fetchSchedule(symGroup,weekNumberListAndFirstDate)
        return schedule
    }


}
