package com.dertefter.neticore.features.documents

import com.dertefter.neticore.features.documents.model.DocumentOptionItem
import com.dertefter.neticore.features.documents.model.DocumentRequestItem
import com.dertefter.neticore.local.UserDataStoreManager
import com.dertefter.neticore.network.NetworkClient
import com.dertefter.neticore.network.ResponseType
import com.dertefter.neticore.network.methods.CiuMethods
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NewDocumentFeature(
    private val client: NetworkClient,
    private val userDataStoreManager: UserDataStoreManager
) {

    private val ciuMethods = CiuMethods(client.ciuApiService)
    private val gson = Gson()

    val status = MutableStateFlow(ResponseType.LOADING)

    private val _options = MutableStateFlow<List<DocumentOptionItem>?>(null)
    val options: StateFlow<List<DocumentOptionItem>?> = _options

    suspend fun updateOptionsList() {
        _options.value = null
        val documentList: List<DocumentOptionItem>? = ciuMethods.fetchDocumentOptionList()
        _options.value = documentList
    }

    suspend fun fetchDocumentRequest(value: String): DocumentRequestItem?{
        val d = ciuMethods.getDocumentRequestItem(value)
        return d
    }

    suspend fun claimNewDocument(selectedValue: String, comment: String) : Boolean? {
        val d = ciuMethods.claimNewDocument(selectedValue, comment)
        return d
    }
}
