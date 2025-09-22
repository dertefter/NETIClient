package com.dertefter.neticlient.ui.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticore.NETICore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessagesViewModel @Inject constructor(
    val netiCore: NETICore
) : ViewModel() {
    val inboxFeature = netiCore.inboxFeature

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
