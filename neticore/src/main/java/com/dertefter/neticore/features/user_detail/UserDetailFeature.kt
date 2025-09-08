package com.dertefter.neticore.features.user_detail

import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dertefter.neticore.features.user_detail.model.UserDetail
import com.dertefter.neticore.features.user_detail.model.lks
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

class UserDetailFeature(
    val client: NetworkClient,
    val userDataStoreManager: UserDataStoreManager
) {

    private val ciuMethods = CiuMethods(client.ciuApiService)
    private val mobileMethods = MobileMethods(client.mobileApiService)
    private val gson = Gson()

    val USER_DETAIL_KEY = stringPreferencesKey("user_detail")

    val USER_DETAIL_MOBILE_KEY = stringPreferencesKey("user_detail_mobile")
    val CURRENT_GROUP_KEY = stringPreferencesKey("current_group")
    val GROUP_HISTORY_KEY = stringPreferencesKey("group_history")

    val LKS_LIST_KEY = stringPreferencesKey("lks_list")

    val status = MutableStateFlow(ResponseType.LOADING)

    val statusLks = MutableStateFlow(ResponseType.LOADING)

    val statusMobile = MutableStateFlow(ResponseType.LOADING)

    @OptIn(ExperimentalCoroutinesApi::class)
    val userDetail: Flow<UserDetail?> = userDataStoreManager.currentStore
        .flatMapLatest { dataStore ->
            dataStore.data.map { prefs ->
                prefs[USER_DETAIL_KEY]?.let { json ->
                    try {
                        gson.fromJson(json, UserDetail::class.java)
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    val userDetailMobile: Flow<UserDetail?> = userDataStoreManager.currentStore
        .flatMapLatest { dataStore ->
            dataStore.data.map { prefs ->
                prefs[USER_DETAIL_MOBILE_KEY]?.let { json ->
                    try {
                        gson.fromJson(json, UserDetail::class.java)
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentGroup: Flow<String?> = userDataStoreManager.currentStore
        .flatMapLatest { dataStore ->
            dataStore.data.map { prefs -> prefs[CURRENT_GROUP_KEY] }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    val groupHistory: Flow<List<String>> = userDataStoreManager.currentStore
        .flatMapLatest { dataStore ->
            dataStore.data.map { prefs ->
                prefs[GROUP_HISTORY_KEY]?.let { json ->
                    try {
                        val type = object : TypeToken<List<String>>() {}.type
                        gson.fromJson<List<String>>(json, type)
                    } catch (e: Exception) {
                        emptyList()
                    }
                } ?: emptyList()
            }
        }


    @OptIn(ExperimentalCoroutinesApi::class)
    val lksList: Flow<List<lks>> = userDataStoreManager.currentStore
        .flatMapLatest { dataStore ->
            dataStore.data.map { prefs ->
                prefs[LKS_LIST_KEY]?.let { json ->
                    try {
                        val type = object : TypeToken<List<lks>>() {}.type
                        gson.fromJson<List<lks>>(json, type)
                    } catch (e: Exception) {
                        emptyList()
                    }
                } ?: emptyList()
            }
        }




    suspend fun setCurrentGroup(group: String) {
        addGroupToHistory(group)
        userDataStoreManager.currentStore.value.edit { prefs ->
            prefs[CURRENT_GROUP_KEY] = group
        }
    }

    suspend fun addGroupToHistory(group: String) {
        userDataStoreManager.currentStore.value.edit { prefs ->
            val currentHistory = prefs[GROUP_HISTORY_KEY]?.let { json ->
                try {
                    val type = object : TypeToken<MutableList<String>>() {}.type
                    gson.fromJson<MutableList<String>>(json, type)
                } catch (e: Exception) {
                    Log.e("groupHistory", e.stackTraceToString())
                    mutableListOf()
                }
            } ?: mutableListOf()

            if (!currentHistory.contains(group)) {
                currentHistory.add(group)
            }

            prefs[GROUP_HISTORY_KEY] = gson.toJson(currentHistory)
        }
    }

    suspend fun removeGroupFromHistory(group: String) {
        userDataStoreManager.currentStore.value.edit { prefs ->
            val currentHistory = prefs[GROUP_HISTORY_KEY]?.let { json ->
                try {
                    val type = object : TypeToken<MutableList<String>>() {}.type
                    gson.fromJson<MutableList<String>>(json, type)
                } catch (e: Exception) {
                    mutableListOf()
                }
            } ?: mutableListOf()

            currentHistory.remove(group)

            prefs[GROUP_HISTORY_KEY] = gson.toJson(currentHistory)
        }
    }

    suspend fun updateUserDetail(){
        updateUserDetailCiu()
        updateUserDetailMobile()
    }


    suspend fun updateUserDetailCiu() {
        status.value = ResponseType.LOADING

        val userDetail = ciuMethods.fetchUserDetail()

        val storeLogin = userDataStoreManager.getDataStoreName()

        if (userDetail != null) {
            userDataStoreManager.currentStore.value.edit { prefs ->
                prefs[USER_DETAIL_KEY] = gson.toJson(
                    userDetail.apply {
                        login = storeLogin
                    }
                )
            }
            status.value = ResponseType.SUCCESS

            if (currentGroup.first() == null && !userDetail.symGroup.isNullOrEmpty()) {
                setCurrentGroup(userDetail.symGroup)
            }

            if (currentGroup.first() == null && !userDetail.otherGroups.isNullOrEmpty()) {
                userDetail.otherGroups.forEach { group ->
                    addGroupToHistory(group)
                }
            }

        } else {
            status.value = ResponseType.ERROR
        }
    }


    suspend fun updateLksList() {
        statusLks.value = ResponseType.LOADING

        val lks = ciuMethods.fetchLksList()

        if (lks != null) {
            userDataStoreManager.currentStore.value.edit { prefs ->
                prefs[LKS_LIST_KEY] = gson.toJson(lks)
            }
            statusLks.value = ResponseType.SUCCESS
        } else {
            statusLks.value = ResponseType.ERROR
        }
    }

    suspend fun setLksById(id: Int) {
        statusLks.value = ResponseType.LOADING
        Log.e("setLksById", "setLksById: $id")
        ciuMethods.setLksById(id)

        updateLksList()
        updateUserDetail()
    }


    suspend fun updateUserDetailMobile() {
        statusMobile.value = ResponseType.LOADING

        val userDetail = mobileMethods.fetchUserDetail()



        val storeLogin = userDataStoreManager.getDataStoreName()

        if (userDetail != null) {
            userDataStoreManager.currentStore.value.edit { prefs ->
                prefs[USER_DETAIL_MOBILE_KEY] = gson.toJson(
                    userDetail.apply {
                        login = storeLogin
                    }
                )
            }
            statusMobile.value = ResponseType.SUCCESS

            if (currentGroup.first() == null && !userDetail.symGroup.isNullOrEmpty()) {
                addGroupToHistory(userDetail.symGroup)
            }

        } else {
            statusMobile.value = ResponseType.ERROR
        }
    }
}
