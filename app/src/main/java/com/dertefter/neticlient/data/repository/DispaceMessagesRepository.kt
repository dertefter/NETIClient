package com.dertefter.neticlient.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.dertefter.neticlient.data.network.NetworkClient
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.network.model.ResponseType
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