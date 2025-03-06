package com.dertefter.neticlient.ui.messages

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.data.repository.MessagesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val messagesRepository: MessagesRepository,
): ViewModel() {

    val messagesTab1 = MutableLiveData<ResponseResult>()
    val messagesTab2 = MutableLiveData<ResponseResult>()


    fun updateMessages(tab: String) {
        viewModelScope.launch {
            getLiveDataForTab(tab).postValue(ResponseResult(ResponseType.LOADING))
            val result = messagesRepository.fetchMessages(tab)
            if (result.responseType == ResponseType.SUCCESS && result.data != null){
                getLiveDataForTab(tab).postValue(result)
            } else {
                val local = messagesRepository.getSavedMessages(tab).first()
                if (local != null){
                    getLiveDataForTab(tab).postValue(
                        ResponseResult(ResponseType.SUCCESS, data = local)
                    )
                } else {
                    getLiveDataForTab(tab).postValue(result)
                }
            }

        }
    }

    fun getMessages(tab: String) {
        viewModelScope.launch {
            val result = messagesRepository.getSavedMessages(tab).first()
            getLiveDataForTab(tab).postValue(
                ResponseResult(ResponseType.SUCCESS, data = result)
            )
        }
    }


    fun getLiveDataForTab(tab: String): MutableLiveData<ResponseResult> {
        val liveData = if (tab  == "tabs1-messages"){
            messagesTab1
        } else {
            messagesTab2
        }
        return liveData
    }


}