package com.dertefter.neticore.features.inbox

import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dertefter.neticore.features.inbox.model.Message
import com.dertefter.neticore.local.UserDataStoreManager
import com.dertefter.neticore.network.NetworkClient
import com.dertefter.neticore.network.ResponseType
import com.dertefter.neticore.network.methods.MobileMethods
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class InboxFeature(
    val client: NetworkClient,
    val userDataStoreManager: UserDataStoreManager
) {

    private val mobileMethods = MobileMethods(client.mobileApiService)
    private val gson = Gson()

    val MESSAGES_LIST_KEY = stringPreferencesKey("messages_list")

    val status = MutableStateFlow(ResponseType.LOADING)

    @OptIn(ExperimentalCoroutinesApi::class)
    val messagesList: Flow<List<Message>?> = userDataStoreManager.currentStore
        .flatMapLatest { dataStore ->
            dataStore.data.map { prefs ->
                prefs[MESSAGES_LIST_KEY]?.let { json ->
                    try {
                        val type = object : TypeToken<List<Message>>() {}.type
                        gson.fromJson<List<Message>>(json, type)
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        }

    private suspend fun saveMessages(messages: List<Message>) {
        userDataStoreManager.currentStore.first().edit { prefs ->
            try {
                val json = gson.toJson(messages)
                prefs[MESSAGES_LIST_KEY] = json
            } catch (e: Exception) {
                Log.e("InboxFeature", "Failed to save messages", e)
            }
        }
    }

    suspend fun updateMessagesList() {
        status.value = ResponseType.LOADING

        val messages = mobileMethods.fetchMessages()

        if (messages != null) {
            status.value = ResponseType.SUCCESS
            saveMessages(messages)
        } else {
            status.value = ResponseType.ERROR
        }
    }

    suspend fun updateMessageReadStatus(idStudent: Int, idMessage: Int, isRead: Int) {

        mobileMethods.updateMessageReadStatus(idStudent, idMessage, isRead)
        updateMessagesList()
    }
}
