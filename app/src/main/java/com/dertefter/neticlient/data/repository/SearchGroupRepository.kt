package com.dertefter.neticlient.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.dertefter.neticlient.common.Constants
import com.dertefter.neticlient.data.model.schedule.Schedule
import com.dertefter.neticlient.data.network.NetworkClient
import com.dertefter.neticlient.data.network.model.ResponseResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SearchGroupRepository @Inject constructor(
    private val networkClient: NetworkClient
) {

    suspend fun fetchGroupList(group: String): ResponseResult {
        val responseResult = networkClient.getGroupList(group)
        return responseResult
    }

}