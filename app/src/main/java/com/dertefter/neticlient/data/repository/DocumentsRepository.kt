package com.dertefter.neticlient.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dertefter.neticlient.data.model.documents.DocumentRequestItem
import com.dertefter.neticlient.data.model.documents.DocumentsItem
import com.dertefter.neticlient.data.network.NetworkClient
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.network.model.ResponseType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class DocumentsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val networkClient: NetworkClient
) {

    suspend fun updateDocumentList() {
        val documentList = networkClient.getDocumentList()
        if (documentList != null){
            saveDocumentList(documentList)
        }
    }

    private suspend fun saveDocumentList(documentList: List<DocumentsItem>) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("document_list")] = Gson().toJson(documentList)
        }
    }

    fun getDocumentListFlow(): Flow<List<DocumentsItem>?> {
        return dataStore.data
            .map { preferences ->
                val json_ = preferences[stringPreferencesKey("document_list")]
                try {
                    json_?.let {
                        val type = object : TypeToken<List<DocumentsItem>>() {}.type
                        Gson().fromJson<List<DocumentsItem>>(it, type)
                    }
                } catch (e: Exception) {
                    null
                }
            }
            .distinctUntilChanged()
    }


    suspend fun fetchOptionsList(): ResponseResult{
        val documentList = networkClient.getDocumentOptionList()
        if (documentList != null){
            return ResponseResult(ResponseType.SUCCESS, data = documentList)
        }
        return ResponseResult(ResponseType.ERROR, "")
    }


    suspend fun fetchDocumentRequest(value: String): DocumentRequestItem?{
        val d = networkClient.getDocumentRequestItem(value)
        return d
    }

    suspend fun claimNewDocument(typeClaim: String, comment: String): Boolean? {
        val d = networkClient.claimNewDocument(typeClaim, comment)
        return d
    }

    suspend fun checkCancelable(id: String): Boolean? {
        val c = networkClient.checkCancelable(id)
        return c
    }

    suspend fun cancelDocument(id: String?): Boolean? {
        val c = networkClient.cancelDocument(id)
        return c
    }

}