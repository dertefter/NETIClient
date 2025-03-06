package com.dertefter.neticlient.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dertefter.neticlient.common.Constants
import com.dertefter.neticlient.data.model.User
import com.dertefter.neticlient.data.model.UserInfo
import com.dertefter.neticlient.data.model.messages.Message
import com.dertefter.neticlient.data.network.NetworkClient
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.network.model.ResponseType
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val networkClient: NetworkClient
) {

    suspend fun saveUser(user: User) {
        dataStore.edit { pref ->
            pref[Constants.LOGIN] = user.login
            pref[Constants.PASSWORD] = user.password
            pref[Constants.NAME] = user.name
            pref[Constants.GROUP] = user.group.orEmpty()
            pref[Constants.PROFILE_PIC_PATH] = user.profilePicPath.orEmpty()
        }
    }

    fun getSelectedGroup(): Flow<String?> = dataStore.data.map {
        val selectedGroup = it[Constants.SELECTED_GROUP]
        if (selectedGroup.isNullOrEmpty()){
            null
        }else{
            selectedGroup
        }
    }

    suspend fun satSelectedGroup(selectedGroup: String){
        dataStore.edit { pref ->
            pref[Constants.SELECTED_GROUP] = selectedGroup
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



    fun getUser(): Flow<User?> = dataStore.data.map {
        val login = it[Constants.LOGIN].orEmpty()
        val password = it[Constants.PASSWORD].orEmpty()

        if (login.isEmpty() || password.isEmpty()) {
            null
        } else {
            User(
                login = login,
                password = password,
                name = it[Constants.NAME].orEmpty(),
                group = it[Constants.GROUP],
                profilePicPath = it[Constants.PROFILE_PIC_PATH]
            )
        }
    }


    suspend fun fetchAuth(login: String, password: String): ResponseResult {
        val authResponse = networkClient.authUser(login, password)
        if (authResponse.responseType == ResponseType.SUCCESS) {
            val userResponse = networkClient.getUserInfo()
            val profilePicResponse = networkClient.getProfilePic()
            if (userResponse.responseType == ResponseType.SUCCESS) {
                val profilePicPath = profilePicResponse.data as String
                val userInfo: UserInfo = userResponse.data as UserInfo
                val user = User(
                    login = login,
                    password = password,
                    name = userInfo.name,
                    group = userInfo.group,
                    profilePicPath = profilePicPath
                )
                if (!userInfo.group.isNullOrEmpty()){
                    if (getSelectedGroup().first().isNullOrEmpty()){
                        satSelectedGroup(userInfo.group)
                    }

                }
                saveUser(user)
                return ResponseResult(ResponseType.SUCCESS, "Успешный вход")
            }
            return ResponseResult(ResponseType.ERROR, "Ошибка авторизации")
            }
        return ResponseResult(ResponseType.ERROR, "Ошибка авторизации")
    }
}