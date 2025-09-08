package com.dertefter.neticlient.data.repository

import com.dertefter.neticlient.data.network.NetworkClient
import com.dertefter.neticlient.data.network.model.ResponseResult
import javax.inject.Inject

class SearchGroupRepository @Inject constructor(
    private val networkClient: NetworkClient
) {

    suspend fun fetchGroupList(group: String): ResponseResult {
        val responseResult = networkClient.getGroupList(group)
        return responseResult
    }

}