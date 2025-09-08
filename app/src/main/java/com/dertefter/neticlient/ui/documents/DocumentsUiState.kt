package com.dertefter.neticlient.ui.documents

import com.dertefter.neticlient.data.model.documents.DocumentsItem
import com.dertefter.neticlient.data.network.model.ResponseType

data class DocumentsUiState(
    val responseType: ResponseType = ResponseType.LOADING,
    val documentList: List<DocumentsItem>? = null
)