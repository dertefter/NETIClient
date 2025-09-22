package com.dertefter.neticlient.ui.documents.new_document

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.features.documents.model.DocumentRequestItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewDocumentViewModel @Inject constructor(
    val netiCore: NETICore
) : ViewModel() {

    val newDocumentFeature = netiCore.newDocumentsFeature

    val optionList = newDocumentFeature.options

    val selectedRequestItem = MutableStateFlow<DocumentRequestItem?>(null)

    val claimSuccess = MutableStateFlow<Boolean?>(null)


    fun updateOptionList(){
        viewModelScope.launch {
            selectedRequestItem.value = null
            newDocumentFeature.updateOptionsList()
        }
    }

    fun fetchDocumentRequest(selectedValue: String) {
        viewModelScope.launch {
            selectedRequestItem.value = null
            selectedRequestItem.value = newDocumentFeature.fetchDocumentRequest(selectedValue)
        }
    }

    fun claimNewDocument(selectedValue: String, comment: String) {
        viewModelScope.launch {
            claimSuccess.value = null
            claimSuccess.value = newDocumentFeature.claimNewDocument(selectedValue, comment)
        }
    }

}