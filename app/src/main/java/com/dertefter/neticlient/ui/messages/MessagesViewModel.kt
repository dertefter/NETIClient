package com.dertefter.neticlient.ui.messages

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticlient.data.model.messages.Message
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.data.repository.MessagesRepository
import com.dertefter.neticlient.ui.schedule.ScheduleUiState
import com.dertefter.neticlient.ui.sessia_schedule.ScheduleSessiaUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val messagesRepository: MessagesRepository,
) : ViewModel() {

    private val _tabStates = MutableStateFlow<Map<String, MessagesTabUiState>>(emptyMap())
    val tabStates: StateFlow<Map<String, MessagesTabUiState>> = _tabStates.asStateFlow()

    val newCountTab1 = MutableLiveData<Int>()
    val newCountTab2 = MutableLiveData<Int>()
    val newCountTabAll = MutableLiveData<Int>()

    fun getMessagesFlow(tab: String): Flow<List<Message>?> {
        return messagesRepository.getMessagesFlow(tab)
    }

    fun getUiStateForTab(tab: String): MessagesTabUiState {
        return _tabStates.value[tab] ?: MessagesTabUiState()
    }

    fun updateMessages(tab: String) {
        updateCount()
        viewModelScope.launch {
            _tabStates.value = _tabStates.value.toMutableMap().apply {
                put(tab, MessagesTabUiState(responseType = ResponseType.LOADING, getUiStateForTab(tab).messages))
            }

            try {
                messagesRepository.updateMessages(tab)

                val messages = messagesRepository.getMessagesFlow(tab).first()

                _tabStates.value = _tabStates.value.toMutableMap().apply {
                    put(tab, MessagesTabUiState(
                        responseType = ResponseType.SUCCESS,
                        messages = messages
                    ))
                }
            } catch (e: Exception) {
                _tabStates.value = _tabStates.value.toMutableMap().apply {
                    put(tab, MessagesTabUiState(
                        responseType = ResponseType.ERROR,
                        messages = null
                    ))
                }
            }
        }
    }

    fun updateCount() {
        viewModelScope.launch {
            val countList = messagesRepository.fetchCount()
            if (countList?.size == 3) {
                newCountTab1.postValue(countList[0])
                newCountTab2.postValue(countList[1])
                newCountTabAll.postValue(countList[2])
            }
        }
    }
}
