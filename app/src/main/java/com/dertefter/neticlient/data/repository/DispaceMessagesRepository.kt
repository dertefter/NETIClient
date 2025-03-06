package com.dertefter.neticlient.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dertefter.neticlient.common.Constants
import com.dertefter.neticlient.data.model.messages.Message
import com.dertefter.neticlient.data.model.schedule.Schedule
import com.dertefter.neticlient.data.network.NetworkClient
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.network.model.ResponseType
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DispaceMessagesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val networkClient: NetworkClient
) {


    suspend fun fetchCompanionList(): ResponseResult {
        val data = networkClient.getDispaceSenderList()
        if (data != null){
            return ResponseResult(ResponseType.SUCCESS, data = data)
        }else {
            return ResponseResult(ResponseType.ERROR, "Ошибка получения списка собеседников")
        }
    }


}