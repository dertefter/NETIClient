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

class RandomPhotoRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val networkClient: NetworkClient
) {


    suspend fun updatePhotoList() {
        val photoList = networkClient.getPhotoList()
        if (photoList != null){
            savePhotoList(photoList)
        }

    }

    suspend fun savePhotoList(promoList: List<String>){
        val _json = Gson().toJson(promoList)
        dataStore.edit { pref ->
            pref[stringPreferencesKey("randomPhotos")] = _json
        }
    }

    fun getPhotoListFlow(): Flow<List<String>?> = dataStore.data.map { preferences ->
        preferences[stringPreferencesKey("randomPhotos")]?.let { json ->
            val listType = object : TypeToken<List<String>>() {}.type
            Gson().fromJson(json, listType)
        }
    }

}