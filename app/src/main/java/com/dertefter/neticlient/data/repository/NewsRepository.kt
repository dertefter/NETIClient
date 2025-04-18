package com.dertefter.neticlient.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.dertefter.neticlient.data.model.messages.Message
import com.dertefter.neticlient.data.model.news.NewsResponse
import com.dertefter.neticlient.data.network.NetworkClient
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.network.model.ResponseType
import javax.inject.Inject

class NewsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val networkClient: NetworkClient
) {

    suspend fun fetchNews(page: Int): ResponseResult {
        val response = networkClient.getNews(page)
        if (response != null){
            return ResponseResult(ResponseType.SUCCESS, data = response)
        }
        return ResponseResult(ResponseType.ERROR)
    }

    suspend fun fetchDetailNews(id: String): ResponseResult {
        val response = networkClient.getNewsDetail(id)
        if (response != null){
            return ResponseResult(ResponseType.SUCCESS, data = response)
        }
        return ResponseResult(ResponseType.ERROR)
    }

    suspend fun fetchPromoList(): ResponseResult {
        val response = networkClient.getPromoList()
        if (response != null){
            return ResponseResult(ResponseType.SUCCESS, data = response)
        }
        return ResponseResult(ResponseType.ERROR)
    }

}