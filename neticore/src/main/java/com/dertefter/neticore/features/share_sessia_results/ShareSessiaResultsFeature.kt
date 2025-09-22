package com.dertefter.neticore.features.share_sessia_results

import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dertefter.neticore.features.sessia_results.model.SessiaResults
import com.dertefter.neticore.features.user_detail.model.UserDetail
import com.dertefter.neticore.local.UserDataStoreManager
import com.dertefter.neticore.network.NetworkClient
import com.dertefter.neticore.network.ResponseType
import com.dertefter.neticore.network.methods.CiuMethods
import com.dertefter.neticore.network.methods.MobileMethods
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class ShareSessiaResultsFeature(
    val client: NetworkClient,
    val userDataStoreManager: UserDataStoreManager
) {

    private val ciuMethods = CiuMethods(client.ciuApiService)
    private val gson = Gson()

    val SHARE_SCORE_LINK_KEY = stringPreferencesKey("share_score_link")

    val status = MutableStateFlow(ResponseType.LOADING)

    @OptIn(ExperimentalCoroutinesApi::class)
    val shareScoreLink: Flow<String?> = userDataStoreManager.currentStore
        .flatMapLatest { dataStore -> dataStore.data.map { prefs -> prefs[SHARE_SCORE_LINK_KEY] } }



    suspend fun updateLink() {
        status.value = ResponseType.LOADING

        val link = ciuMethods.fetchShareScore()

        if (link != null) {
            userDataStoreManager.currentStore.value.edit { prefs ->
                prefs[SHARE_SCORE_LINK_KEY] = link
            }
            status.value = ResponseType.SUCCESS

        } else {
            status.value = ResponseType.ERROR
        }
    }

    suspend fun requestNewLink() {

        status.value = ResponseType.LOADING

        ciuMethods.fetchSessiaResults()

        updateLink()
    }
}
