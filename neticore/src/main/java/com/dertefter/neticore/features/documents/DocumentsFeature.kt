package com.dertefter.neticore.features.documents

import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dertefter.neticore.features.documents.model.DocumentsItem
import com.dertefter.neticore.features.inbox.model.Message
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

class DocumentsFeature(
    val client: NetworkClient,
    val userDataStoreManager: UserDataStoreManager
) {

    private val ciuMethods = CiuMethods(client.ciuApiService)
    private val gson = Gson()

    val DOCS_LIST_KEY = stringPreferencesKey("documents_list")

    val status = MutableStateFlow(ResponseType.LOADING)

    @OptIn(ExperimentalCoroutinesApi::class)
    val documentList: Flow<List<DocumentsItem>?> = userDataStoreManager.currentStore
        .flatMapLatest { dataStore ->
            dataStore.data.map { prefs ->
                prefs[DOCS_LIST_KEY]?.let { json ->
                    try {
                        val type = object : TypeToken<List<DocumentsItem>>() {}.type
                        gson.fromJson<List<DocumentsItem>>(json, type)
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        }

    private suspend fun saveDocuments(documents: List<DocumentsItem>) {
        userDataStoreManager.currentStore.first().edit { prefs ->
            try {
                val json = gson.toJson(documents)
                prefs[DOCS_LIST_KEY] = json
            } catch (e: Exception) {
                Log.e("DocumentsFeature", "Failed to save documents", e)
            }
        }
    }

    suspend fun updateDocumentList() {
        status.value = ResponseType.LOADING
        val documents = ciuMethods.fetchDocumentList()
        if (documents != null) {
            status.value = ResponseType.SUCCESS
            saveDocuments(documents)
        } else {
            status.value = ResponseType.ERROR
        }
    }


    suspend fun checkCancelable(id: String): Boolean? {
        val c = ciuMethods.checkCancelable(id)
        return c
    }

    suspend fun cancelDocument(id: String?): Boolean? {
        val c = ciuMethods.cancelDocument(id)
        updateDocumentList()
        return c
    }

}
