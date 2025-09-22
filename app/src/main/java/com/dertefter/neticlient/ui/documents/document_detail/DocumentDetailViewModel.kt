package com.dertefter.neticlient.ui.documents.document_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticore.NETICore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DocumentDetailViewModel @Inject constructor(
    val netiCore: NETICore
) : ViewModel() {

    val documentsFeature = netiCore.documentsFeature

    val cancelable = MutableStateFlow<Boolean?>(null)

    val cancelSuccess = MutableStateFlow<Boolean?>(null)


    fun updateCancelable(id: String){
        viewModelScope.launch {
            cancelable.value = documentsFeature.checkCancelable(id)
        }
    }

    fun cancelDocument(id: String){
        viewModelScope.launch {
            cancelSuccess.value = documentsFeature.cancelDocument(id)
        }

    }

}