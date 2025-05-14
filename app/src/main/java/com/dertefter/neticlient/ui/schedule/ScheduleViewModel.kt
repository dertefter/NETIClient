package com.dertefter.neticlient.ui.schedule

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticlient.data.model.CurrentTimeObject
import com.dertefter.neticlient.data.model.schedule.Schedule
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.data.repository.ScheduleRepository
import com.dertefter.neticlient.data.repository.UserRepository
import com.dertefter.neticlient.ui.sessia_schedule.ScheduleSessiaUiState
import com.dertefter.neticlient.widgets.schedule_widget.ScheduleWidget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.getOrPut

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val userRepository: UserRepository
): ViewModel() {

    fun observeScheduleAndUpdateWidget(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            userRepository.getSelectedGroupFlow()
                .filter { !it.isNullOrEmpty() }
                .flatMapLatest { group -> scheduleRepository.getScheduleFlow(group.orEmpty()) }
                .collect {
                    val intent = Intent(context, ScheduleWidget::class.java).apply {
                        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    }

                    val ids = AppWidgetManager.getInstance(context)
                        .getAppWidgetIds(ComponentName(context, ScheduleWidget::class.java))

                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                    context.sendBroadcast(intent)
                }
        }
    }





    init {
        observeSelectedGroup()
    }

    private fun observeSelectedGroup() {
        viewModelScope.launch {
            userRepository.getSelectedGroupFlow()
                .filterNotNull()
                .collectLatest { group ->
                    updateSchedule(group)
                    updateScheduleSessia(group)
                }
        }
    }


    val groupHistoryLiveData = MutableLiveData<List<String>>()


    private val _scheduleState = MutableStateFlow(ScheduleUiState())
    val scheduleState: StateFlow<ScheduleUiState> = _scheduleState.asStateFlow()

    private val _scheduleSessiaState = MutableStateFlow(ScheduleSessiaUiState())
    val scheduleSessiaState: StateFlow<ScheduleSessiaUiState> = _scheduleSessiaState.asStateFlow()

    val weekNumberFlow = scheduleRepository.getWeekNumberFlow()

    val weekLabelFlow = scheduleRepository.getWeekLabelFlow()

    fun updateWeekNumber(){
        viewModelScope.launch {
            scheduleRepository.updateWeekNumber()
        }
    }


    fun updateSchedule(group: String) {
        viewModelScope.launch {

            if (_scheduleState.value.group != group || _scheduleState.value.schedule == null){
                _scheduleState.value = ScheduleUiState(responseType = ResponseType.LOADING, group = group)
            }

            val user = userRepository.getUser().first()
            val isIndividual = user?.group == group

            try {
                scheduleRepository.updateSchedule(group, isIndividual)

                val schedule = scheduleRepository.getScheduleFlow(group).first()

                _scheduleState.value = ScheduleUiState(
                    responseType = ResponseType.SUCCESS,
                    group = group,
                    schedule = schedule
                )
            } catch (e: Exception) {
                _scheduleState.value = ScheduleUiState(
                    responseType = ResponseType.ERROR,
                    group = group,
                    schedule = null
                )
            }
        }
    }

    fun updateScheduleSessia(group: String) {
        viewModelScope.launch {

            if (_scheduleSessiaState.value.group != group || _scheduleSessiaState.value.schedule == null){
                _scheduleSessiaState.value = ScheduleSessiaUiState(responseType = ResponseType.LOADING, group = group)
            }

            try {
                scheduleRepository.updateScheduleSessia(group)

                val schedule = scheduleRepository.getScheduleSessiaFlow(group).first()

                _scheduleSessiaState.value = ScheduleSessiaUiState(
                    responseType = ResponseType.SUCCESS,
                    group = group,
                    schedule = schedule
                )
            } catch (e: Exception) {
                _scheduleSessiaState.value = ScheduleSessiaUiState(
                    responseType = ResponseType.ERROR,
                    group = group,
                    schedule = null
                )
            }
        }
    }

    fun getGroupHistory(){
        viewModelScope.launch {
            val history = userRepository.getGroupHistory().first()
            groupHistoryLiveData.postValue(history)
        }
    }

    fun addGroupToHistory(group: String){
        viewModelScope.launch {
            userRepository.addGroupToHistory(group)
            getGroupHistory()
        }
    }

    fun removeGroupFromHistory(group: String){
        viewModelScope.launch {
            userRepository.removeGroupFromHistory(group)
            getGroupHistory()
        }
    }

    fun updateSelectedGroup(group: String){
        viewModelScope.launch {
            userRepository.updateSelectedGroup(group)
        }
    }

    fun updateWeekLabel(){
        viewModelScope.launch {
            scheduleRepository.updateWeekLabel()
        }
    }



}