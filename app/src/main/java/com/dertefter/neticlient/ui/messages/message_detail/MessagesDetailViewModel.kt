package com.dertefter.neticlient.ui.messages.message_detail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.data.repository.CourcesRepository
import com.dertefter.neticlient.data.repository.MessagesRepository
import com.dertefter.neticlient.data.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessagesDetailViewModel @Inject constructor(
    private val messagesRepository: MessagesRepository
): ViewModel() {

    val messageDetailLiveData = MutableLiveData<ResponseResult>()

    fun fetchMessageDetail(id: String){
        viewModelScope.launch {
            messageDetailLiveData.postValue(ResponseResult(ResponseType.LOADING))
            val responseResult = messagesRepository.fetchMessageDetail(id)
            messageDetailLiveData.postValue(responseResult)
        }
    }

}