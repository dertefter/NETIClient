package com.dertefter.neticlient.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.dertefter.neticlient.common.Constants
import com.dertefter.neticlient.data.model.User
import com.dertefter.neticlient.data.model.UserInfo
import com.dertefter.neticlient.data.model.profile_detail.ProfileDetail
import com.dertefter.neticlient.data.network.NetworkClient
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.network.model.ResponseType
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val networkClient: NetworkClient
) {

    fun getSelectedGroupFlow(): Flow<String?> = dataStore.data
        .map {
        val selectedGroup = it[Constants.SELECTED_GROUP]
        if (selectedGroup.isNullOrEmpty()){
            null
        }else{
            selectedGroup
        }
      }.distinctUntilChanged()

    suspend fun updateSelectedGroup(selectedGroup: String){
        dataStore.edit { pref ->
            pref[Constants.SELECTED_GROUP] = selectedGroup
        }
        addGroupToHistory(group = selectedGroup)
    }

    suspend fun saveUser(user: User) {
        val userJson = Gson().toJson(user)
        dataStore.edit { pref ->
            pref[Constants.USER] = userJson
        }
    }

    suspend fun removeUser() {
        dataStore.edit { pref ->
            pref.remove(Constants.USER)
            networkClient.rebuildClientWithToken(null)
        }
    }

    fun getUser(): Flow<User?> = dataStore.data.map { preferences ->
        preferences[Constants.USER]?.let { json ->
            Gson().fromJson(json, User::class.java)
        }
    }




    suspend fun addGroupToHistory(group: String) {
        dataStore.edit { preferences ->
            val history = getGroupHistory().first().toMutableList()
            if (!history.contains(group)){
                history.add(group)
                preferences[Constants.GROUP_HISTORY] = Gson().toJson(history)
            }
        }
    }

    suspend fun removeGroupFromHistory(group: String){
        dataStore.edit { preferences ->
            val history = getGroupHistory().first().toMutableList()
            history.remove(group)
            preferences[Constants.GROUP_HISTORY] = Gson().toJson(history)
        }
    }

    fun getGroupHistory(): Flow<List<String>> {
        return dataStore.data.map { preferences ->
            val messagesJson = preferences[Constants.GROUP_HISTORY]
            if (messagesJson != null) {
                Gson().fromJson(messagesJson, Array<String>::class.java).toList()
            } else {
                emptyList()
            }
        }
    }


    suspend fun fetchAuth(login: String, password: String): ResponseResult {
        val authResponse = networkClient.authUser(login, password)
        if (authResponse.responseType == ResponseType.SUCCESS) {
            val userResponse = networkClient.getUserInfo()
            val profilePicResponse = networkClient.getProfilePic()
            if (userResponse.responseType == ResponseType.SUCCESS) {
                val profilePicPath = profilePicResponse.data as String
                val userInfo: UserInfo = userResponse.data as UserInfo?
                    ?: return ResponseResult(ResponseType.ERROR)

                val user = User(
                    login = login,
                    password = password,
                    name = userInfo.name,
                    group = userInfo.group,
                    profilePicPath = profilePicPath,
                )
                if (!userInfo.group.isNullOrEmpty()){
                    if (getSelectedGroupFlow().first().isNullOrEmpty()){
                        updateSelectedGroup(userInfo.group)
                    }

                }
                saveUser(user)
                return ResponseResult(ResponseType.SUCCESS, "Успешный вход")
            }
            return ResponseResult(ResponseType.ERROR, "Ошибка авторизации")
            }
        return ResponseResult(ResponseType.ERROR, "Ошибка авторизации")
    }

    suspend fun fetchProfileDetail(): ResponseResult {
        val details = networkClient.fetchProfileDetail()
        if (details != null){
            return ResponseResult(ResponseType.SUCCESS, data = details as ProfileDetail)
        }else {
            return ResponseResult(ResponseType.ERROR)
        }
    }

    suspend fun saveProfileDetails(
        n_email: String,
        n_address: String,
        n_phone: String,
        n_snils: String,
        n_oms: String,
        n_vk: String,
        n_tg: String,
        n_leader: String
    ): ResponseResult {
        val details = networkClient.saveProfileDetail(n_email, n_address, n_phone, n_snils, n_oms, n_vk, n_tg, n_leader)
        if (details != null){
            return ResponseResult(ResponseType.SUCCESS, data = details)
        }else {
            return ResponseResult(ResponseType.ERROR)
        }
    }


}