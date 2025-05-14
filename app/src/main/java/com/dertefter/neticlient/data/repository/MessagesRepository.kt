package com.dertefter.neticlient.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dertefter.neticlient.common.Constants
import com.dertefter.neticlient.data.model.messages.Message
import com.dertefter.neticlient.data.model.schedule.Schedule
import com.dertefter.neticlient.data.model.sessia_schedule.SessiaScheduleItem
import com.dertefter.neticlient.data.network.NetworkClient
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.network.model.ResponseType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MessagesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val networkClient: NetworkClient
) {


    suspend fun fetchMessageDetail(id: String): ResponseResult {
        val messageDetail = networkClient.getMessageDetail(id)
        if (messageDetail != null) {
            return ResponseResult(ResponseType.SUCCESS, data = messageDetail)
        }
        return ResponseResult(ResponseType.ERROR, data = messageDetail)
    }

    suspend fun updateMessages(tab: String) {
        val response = networkClient.getMessages(tab)
        if (response.responseType == ResponseType.SUCCESS) {
            if (!(response.data as List<Message>?).isNullOrEmpty()){
                saveMessages(tab, response.data as List<Message>)
            }
        }
    }

    private suspend fun saveMessages(tab: String, messages: List<Message>) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("messages_$tab")] = Gson().toJson(messages)
        }
    }

    fun getMessagesFlow(tab: String): Flow<List<Message>?> {
        return dataStore.data
            .map { preferences ->
                val json_ = preferences[stringPreferencesKey("messages_$tab")]
                try {
                    json_?.let {
                        val type = object : TypeToken<List<Message>>() {}.type
                        Gson().fromJson<List<Message>>(it, type)
                    }
                } catch (e: Exception) {
                    null
                }
            }
            .distinctUntilChanged()
    }


    suspend fun fetchCount(): List<Int>? {
        val response = networkClient.getMessagesCount()
        if (response.responseType == ResponseType.SUCCESS) {
            return response.data as List<Int>?
        } else {
            return listOf(0, 0, 0)
        }

    }




}