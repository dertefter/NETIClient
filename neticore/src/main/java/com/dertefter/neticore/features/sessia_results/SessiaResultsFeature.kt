package com.dertefter.neticore.features.sessia_results

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

class SessiaResultsFeature(
    val client: NetworkClient,
    val userDataStoreManager: UserDataStoreManager
) {

    private val ciuMethods = CiuMethods(client.ciuApiService)
    private val mobileMethods = MobileMethods(client.mobileApiService)
    private val gson = Gson()

    val SESSIA_RESILTS_KEY = stringPreferencesKey("sessia_results")
    val SESSIA_RESILTS_MOBILE_KEY = stringPreferencesKey("sessia_results_mobile")

    val status = MutableStateFlow(ResponseType.LOADING)
    val statusMobile = MutableStateFlow(ResponseType.LOADING)

    @OptIn(ExperimentalCoroutinesApi::class)
    val sessiaResults: Flow<SessiaResults?> = userDataStoreManager.currentStore
        .flatMapLatest { dataStore ->
            dataStore.data.map { prefs ->
                prefs[SESSIA_RESILTS_KEY]?.let { json ->
                    try {
                        gson.fromJson(json, SessiaResults::class.java)
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    val sessiaResultsMobile: Flow<SessiaResults?> = userDataStoreManager.currentStore
        .flatMapLatest { dataStore ->
            dataStore.data.map { prefs ->
                prefs[SESSIA_RESILTS_MOBILE_KEY]?.let { json ->
                    try {
                        gson.fromJson(json, SessiaResults::class.java)
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        }


    suspend fun updateSesiaResults(){
        updateSesiaResultsCiu()
        updateSesiaResultsMobile()
    }


    suspend fun updateSesiaResultsCiu() {
        status.value = ResponseType.LOADING

        val sessiaResults = ciuMethods.fetchSessiaResults()

        Log.e("sess", sessiaResults.toString())

        if (sessiaResults != null) {
            userDataStoreManager.currentStore.value.edit { prefs ->
                prefs[SESSIA_RESILTS_KEY] = gson.toJson(sessiaResults)
            }
            status.value = ResponseType.SUCCESS

        } else {
            status.value = ResponseType.ERROR
        }
    }

    suspend fun updateSesiaResultsMobile() {
        statusMobile.value = ResponseType.LOADING
        statusMobile.value = ResponseType.ERROR
        //TODO
    }
}
