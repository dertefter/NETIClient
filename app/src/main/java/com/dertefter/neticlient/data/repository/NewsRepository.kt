package com.dertefter.neticlient.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dertefter.neticlient.data.model.messages.Message
import com.dertefter.neticlient.data.model.news.NewsResponse
import com.dertefter.neticlient.data.model.news.PromoItem
import com.dertefter.neticlient.data.model.sessia_results.SessiaResults
import com.dertefter.neticlient.data.network.NetworkClient
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.network.model.ResponseType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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

    suspend fun updatePromoList() {
        val promoList = networkClient.getPromoList()
        if (promoList != null){
            savePromoList(promoList)
        }

    }

    suspend fun savePromoList(promoList: List<PromoItem>){
        val _json = Gson().toJson(promoList)
        dataStore.edit { pref ->
            pref[stringPreferencesKey("promoList")] = _json
        }
    }

    fun getPromoListFlow(): Flow<List<PromoItem>?> = dataStore.data.map { preferences ->
        preferences[stringPreferencesKey("promoList")]?.let { json ->
            val listType = object : TypeToken<List<PromoItem>>() {}.type
            Gson().fromJson(json, listType)
        }
    }


    suspend fun fetchDetailNews(id: String): ResponseResult {
        val response = networkClient.getNewsDetail(id)
        if (response != null){
            return ResponseResult(ResponseType.SUCCESS, data = response)
        }
        return ResponseResult(ResponseType.ERROR)
    }


}