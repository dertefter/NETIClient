package com.dertefter.neticlient.ui.messages.message_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticore.NETICore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MessagesDetailViewModel @Inject constructor(
    val netiCore: NETICore
) : ViewModel() {
    val inboxFeature = netiCore.inboxFeature

    fun readMessage(idStudent: Int, idMessage: Int, isRead: Int){
        viewModelScope.launch {
            inboxFeature.updateMessageReadStatus(idStudent, idMessage, isRead)
        }

    }



}