package com.dertefter.neticlient.ui.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticore.NETICore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DocumentsViewModel @Inject constructor(
    val netiCore: NETICore
) : ViewModel() {
    val documentsFeature = netiCore.documentsFeature

    val documentList = documentsFeature.documentList
        .stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    val status = documentsFeature.status


    fun updateDocumentList() {
        viewModelScope.launch {
            documentsFeature.updateDocumentList()
        }
    }




}