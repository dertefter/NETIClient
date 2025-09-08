package com.dertefter.neticlient.ui.messages

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticlient.data.model.messages.Message
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.data.repository.MessagesRepository
import com.dertefter.neticlient.ui.sessia_schedule.ScheduleSessiaUiState
import com.dertefter.neticore.NETICore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


class MessagesViewModel() : ViewModel() {

    val inboxFeature = NETICore.inboxFeature

    val messagesList = inboxFeature.messagesList
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    val status = inboxFeature.status

    fun updateMessages(){
        viewModelScope.launch {
            inboxFeature.updateMessagesList()
        }
    }

}
