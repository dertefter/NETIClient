package com.dertefter.neticlient.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
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

class SessiaResultsRepository @Inject constructor(
    private val networkClient: NetworkClient
) {

    suspend fun fetchSessiaResults(): ResponseResult {
        val sessia_results = networkClient.getSessiaResults()
        if (sessia_results != null){
            return ResponseResult(ResponseType.SUCCESS, data = sessia_results)
        }
        return ResponseResult(ResponseType.ERROR, "Ошибка авторизации")
    }
}