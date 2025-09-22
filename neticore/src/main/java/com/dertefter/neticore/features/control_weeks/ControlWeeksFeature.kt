package com.dertefter.neticore.features.control_weeks

import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dertefter.neticore.features.control_weeks.model.ControlResult
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

class ControlWeeksFeature(
    val client: NetworkClient,
    val userDataStoreManager: UserDataStoreManager
) {

    private val ciuMethods = CiuMethods(client.ciuApiService)
    private val gson = Gson()

    val CONTROL_WEEKS_KEY = stringPreferencesKey("control_weeks")

    val status = MutableStateFlow(ResponseType.LOADING)

    @OptIn(ExperimentalCoroutinesApi::class)
    val controlWeeks: Flow<ControlResult?> = userDataStoreManager.currentStore
        .flatMapLatest { dataStore ->
            dataStore.data.map { prefs ->
                prefs[CONTROL_WEEKS_KEY]?.let { json ->
                    try {
                        gson.fromJson(json, ControlResult::class.java)
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        }




    suspend fun updateControlWeeks() {
        status.value = ResponseType.LOADING

        val controlWeeks = ciuMethods.fetchControlWeeks()

        if (controlWeeks != null) {
            userDataStoreManager.currentStore.value.edit { prefs ->
                prefs[CONTROL_WEEKS_KEY] = gson.toJson(controlWeeks)
            }
            status.value = ResponseType.SUCCESS

        } else {
            status.value = ResponseType.ERROR
        }
    }

}
