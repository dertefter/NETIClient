package com.dertefter.neticore.features.money

import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dertefter.neticore.features.money.model.MoneyItem
import com.dertefter.neticore.local.UserDataStoreManager
import com.dertefter.neticore.network.NetworkClient
import com.dertefter.neticore.network.ResponseType
import com.dertefter.neticore.network.methods.CiuMethods
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class MoneyFeature(
    val client: NetworkClient,
    val userDataStoreManager: UserDataStoreManager
) {

    private val ciuMethods = CiuMethods(client.ciuApiService)
    private val gson = Gson()

    val YEAR_LIST_KEY = stringPreferencesKey("tear_list")

    fun YEAR_MONEY_KEY(s: String) = stringPreferencesKey("money_year_$s")

    val status = MutableStateFlow(ResponseType.LOADING)

    @OptIn(ExperimentalCoroutinesApi::class)
    val yearList: Flow<List<String>?> = userDataStoreManager.currentStore
        .flatMapLatest { dataStore ->
            dataStore.data.map { prefs ->
                prefs[YEAR_LIST_KEY]?.let { json ->
                    try {
                        val type = object : TypeToken<List<String>>() {}.type
                        gson.fromJson<List<String>>(json, type)
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        }


    @OptIn(ExperimentalCoroutinesApi::class)
    fun getMoneyItemsForYear(year: String): Flow<List<MoneyItem>?> {
        return userDataStoreManager.currentStore
            .flatMapLatest { dataStore ->
                dataStore.data.map { prefs ->
                    prefs[YEAR_MONEY_KEY(year)]?.let { json ->
                        try {
                            val type = object : TypeToken<List<MoneyItem>>() {}.type
                            gson.fromJson<List<MoneyItem>>(json, type)
                        } catch (e: Exception) {
                            Log.e("MoneyFeature", "Failed to deserialize money items for year $year", e)
                            null
                        }
                    }
                }
            }
    }


    private suspend fun saveYears(years: List<String>) {
        userDataStoreManager.currentStore.first().edit { prefs ->
            try {
                val json = gson.toJson(years)
                prefs[YEAR_LIST_KEY] = json
            } catch (e: Exception) {
                Log.e("MoneyFeature", "Failed to save ", e)
            }
        }
    }

    suspend fun updateYearList() {
        status.value = ResponseType.LOADING
        val documents = ciuMethods.fetchMoneyYearsList()
        if (documents != null) {
            status.value = ResponseType.SUCCESS
            saveYears(documents)
        } else {
            status.value = ResponseType.ERROR
        }
    }

    suspend fun updateMoneyItemsForYear(year: String) {
        val moneyItems = ciuMethods.fetchMoneyItems(year)
        if (moneyItems != null) {
            saveMoneyItemsForYear(year, moneyItems)
        }
    }

    private suspend fun saveMoneyItemsForYear(year: String, items: List<MoneyItem>) {
        userDataStoreManager.currentStore.first().edit { prefs ->
            try {
                val json = gson.toJson(items)
                prefs[YEAR_MONEY_KEY(year)] = json
            } catch (e: Exception) {
                Log.e("MoneyFeature", "Failed to save money items for year $year", e)
            }
        }
    }



}
