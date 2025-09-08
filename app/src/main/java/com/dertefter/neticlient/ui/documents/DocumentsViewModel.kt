package com.dertefter.neticlient.ui.documents

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticlient.data.model.documents.DocumentRequestItem
import com.dertefter.neticlient.data.model.news.NewsItem
import com.dertefter.neticlient.data.model.news.NewsResponse
import com.dertefter.neticlient.data.model.news.PromoItem
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.data.repository.CourcesRepository
import com.dertefter.neticlient.data.repository.DocumentsRepository
import com.dertefter.neticlient.data.repository.NewsRepository
import com.dertefter.neticlient.ui.sessia_schedule.ScheduleSessiaUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DocumentsViewModel @Inject constructor(
    private val documentsRepository: DocumentsRepository
): ViewModel() {

    private val _documentsState = MutableStateFlow(DocumentsUiState())
    val documentsState: StateFlow<DocumentsUiState> = _documentsState.asStateFlow()

    val optionListLiveData = MutableLiveData<ResponseResult>()

    val selectedRequestItem = MutableLiveData<DocumentRequestItem?>()

    val docCancelable = MutableLiveData<Boolean?>()

    fun updateDocumentList() {
        viewModelScope.launch {
            _documentsState.value = DocumentsUiState(responseType = ResponseType.LOADING, documentsRepository.getDocumentListFlow().first())
            try {
                documentsRepository.updateDocumentList()

                val documentList = documentsRepository.getDocumentListFlow().first()

                _documentsState.value = DocumentsUiState(
                    responseType = ResponseType.SUCCESS,
                    documentList = documentList
                )
            } catch (e: Exception) {
                _documentsState.value = DocumentsUiState(
                    responseType = ResponseType.ERROR,
                    documentList = null
                )
            }
        }
    }


    fun fetchDocumentRequest(value: String){
        viewModelScope.launch {
            val d = documentsRepository.fetchDocumentRequest(value)
            selectedRequestItem.postValue(d)

        }
    }

    fun fetchOptionList(){
        viewModelScope.launch {
            optionListLiveData.postValue(ResponseResult(ResponseType.LOADING))
            val responseResult = documentsRepository.fetchOptionsList()
            optionListLiveData.postValue(responseResult)
        }
    }

    fun claimNewDocument(type_claim: String, comment: String) {
        viewModelScope.launch {
            val responseResult = documentsRepository.claimNewDocument(type_claim, comment)
            updateDocumentList()
        }
    }

    fun checkCancelable(id: String){
        viewModelScope.launch {
            val c = documentsRepository.checkCancelable(id)
            docCancelable.postValue(c)
        }

    }

    fun cancelClaim(id: String?) {
        viewModelScope.launch {
            val c = documentsRepository.cancelDocument(id)
            updateDocumentList()
        }
    }


}