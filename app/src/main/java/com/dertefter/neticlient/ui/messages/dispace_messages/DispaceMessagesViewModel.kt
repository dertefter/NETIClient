package com.dertefter.neticlient.ui.messages.dispace_messages

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.data.repository.DispaceMessagesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DispaceMessagesViewModel @Inject constructor(
    private val dispaceMessagesRepository: DispaceMessagesRepository
): ViewModel() {

    val senderListLiveData = MutableLiveData<ResponseResult>()

    fun fetchSenderList(){
        viewModelScope.launch {
            ResponseResult(ResponseType.LOADING)
            val result = dispaceMessagesRepository.fetchCompanionList()
            senderListLiveData.postValue(
                result
            )
        }
    }




}