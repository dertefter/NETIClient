package com.dertefter.neticlient.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.MutableLiveData
import com.dertefter.neticlient.common.Constants
import com.dertefter.neticlient.data.model.User
import com.dertefter.neticlient.data.model.UserInfo
import com.dertefter.neticlient.data.model.dashboard.DashboardItem
import com.dertefter.neticlient.data.network.NetworkClient
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.network.model.ResponseType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DashboardRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val networkClient: NetworkClient,
) {

    suspend fun getDashboardItemList(): Flow<List<DashboardItem>> {
        return dataStore.data.map { preferences ->
            val json = preferences[Constants.DASHBOARD_ITEMS]
            if (!json.isNullOrEmpty()) {
                val type = object : TypeToken<List<DashboardItem>>() {}.type
                Gson().fromJson<List<DashboardItem>>(json, type) ?: emptyList()
            } else {
                emptyList()
            }
        }
    }

    suspend fun saveDashboardItemList(list: List<DashboardItem>) {
        val json =  Gson().toJson(list)
        dataStore.edit { preferences ->
            preferences[Constants.DASHBOARD_ITEMS] = json
        }
    }

    suspend fun saveHeaderLabel(label: String) {
        dataStore.edit { preferences ->
            preferences[Constants.HEADER_LABEL] = label
        }
    }

    suspend fun getHeaderLabel(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[Constants.HEADER_LABEL]
        }
    }

    suspend fun fetchHeaderLabel(): ResponseResult {
        val response = networkClient.getHeaderLabel()
        if (response != null) {
            saveHeaderLabel(response)
            return ResponseResult(ResponseType.SUCCESS, data = response)
        } else {
            val cachedLabel = getHeaderLabel().first()
            if (!cachedLabel.isNullOrEmpty()) {
                return ResponseResult(ResponseType.SUCCESS, data = cachedLabel)
            }
        }
        return ResponseResult(ResponseType.ERROR, "Ошибка получения заголовка")
    }

    suspend fun addDashboardItem(item: DashboardItem) {
        val currentList = getDashboardItemList().first().toMutableList()
        currentList.add(item)
        saveDashboardItemList(currentList)
    }

    suspend fun removeItem(item: DashboardItem) {
        val currentList = getDashboardItemList().first().toMutableList()
        if (currentList.contains(item)){
            currentList.remove(item)
        }
        saveDashboardItemList(currentList)

    }

    suspend fun moveItem(fromPosition: Int, toPosition: Int) {
        val currentList = getDashboardItemList().first().toMutableList()
        val item = currentList.removeAt(fromPosition)
        currentList.add(toPosition, item)
        saveDashboardItemList(currentList)

    }
}
