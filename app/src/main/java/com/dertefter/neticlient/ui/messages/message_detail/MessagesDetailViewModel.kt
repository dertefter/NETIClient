package com.dertefter.neticlient.ui.messages.message_detail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.data.repository.CourcesRepository
import com.dertefter.neticlient.data.repository.MessagesRepository
import com.dertefter.neticlient.data.repository.NewsRepository
import com.dertefter.neticore.NETICore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class MessagesDetailViewModel: ViewModel() {

    val inboxFeature = NETICore.inboxFeature

    fun readMessage(idStudent: Int, idMessage: Int, isRead: Int){
        viewModelScope.launch {
            inboxFeature.updateMessageReadStatus(idStudent, idMessage, isRead)
        }

    }



}