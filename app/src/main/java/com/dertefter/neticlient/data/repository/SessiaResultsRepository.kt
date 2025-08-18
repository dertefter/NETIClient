package com.dertefter.neticlient.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dertefter.neticlient.common.Constants
import com.dertefter.neticlient.data.model.User
import com.dertefter.neticlient.data.model.UserInfo
import com.dertefter.neticlient.data.model.control_weeks.ControlResult
import com.dertefter.neticlient.data.model.messages.Message
import com.dertefter.neticlient.data.model.profile_detail.ProfileDetail
import com.dertefter.neticlient.data.model.sessia_results.SessiaResults
import com.dertefter.neticlient.data.network.NetworkClient
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.network.model.ResponseType
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SessiaResultsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val networkClient: NetworkClient
) {
    suspend fun updateSessiaResults() {
        val sessiaResults = networkClient.getSessiaResults()
        if (sessiaResults != null && sessiaResults.srScore?.isNaN() == false ){
            saveSessiaResults(sessiaResults)
        }

    }

    suspend fun saveSessiaResults(sessiaResults: SessiaResults){
        val _json = Gson().toJson(sessiaResults)
        dataStore.edit { pref ->
            pref[stringPreferencesKey("sessiaResults")] = _json
        }
    }

    fun getSessiaResultsFlow(): Flow<SessiaResults?> = dataStore.data.map { preferences ->
        preferences[stringPreferencesKey("sessiaResults")]?.let { json ->
            Gson().fromJson(json, SessiaResults::class.java)
        }
    }




    suspend fun updateControlWeeks() {
        val controlResult = networkClient.getControlWeeks()
        if (controlResult != null){
            saveControlWeeks(controlResult)
        }

    }

    suspend fun saveControlWeeks(controlResult: ControlResult){
        val _json = Gson().toJson(controlResult)
        dataStore.edit { pref ->
            pref[stringPreferencesKey("controlWeeks")] = _json
        }
    }

    fun getControlWeeksFlow(): Flow<ControlResult?> = dataStore.data.map { preferences ->
        preferences[stringPreferencesKey("controlWeeks")]?.let { json ->
            Gson().fromJson(json, ControlResult::class.java)
        }
    }


    suspend fun updateShareScore() {
        val shareScore = networkClient.getShareScore()
        if (shareScore != null){
            saveShareScore(shareScore)
        }
    }

    suspend fun replaceLink() {
        networkClient.getShareScoreReplaceLink()
    }

    suspend fun saveShareScore(shareScore: String){
        dataStore.edit { pref ->
            pref[stringPreferencesKey("shareScore")] = shareScore
        }
    }

    fun getShareScoreFlow(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[stringPreferencesKey("shareScore")]
    }


}