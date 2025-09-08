package com.dertefter.neticlient.ui.schedule

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticlient.data.model.CurrentTimeObject
import com.dertefter.neticore.features.schedule.model.Day
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.data.repository.ScheduleRepository
import com.dertefter.neticlient.ui.sessia_schedule.ScheduleSessiaUiState
import com.dertefter.neticlient.widgets.schedule_widget.ScheduleWidget
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.features.schedule.model.Schedule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject


class ScheduleViewModel(): ViewModel() {

    val scheduleFeature = NETICore.scheduleFeature
    val userDetailFeature = NETICore.userDetailFeature
    val currentGroup: StateFlow<String?> =
        userDetailFeature.currentGroup
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = null
            )

    val currentTime:  StateFlow<LocalTime?>  = CurrentTimeObject.currentTimeFlow
    val currentDateFlow:  StateFlow<LocalDate?>  = CurrentTimeObject.currentDateFlow

    val status = scheduleFeature.status

    @OptIn(ExperimentalCoroutinesApi::class)
    val schedule: StateFlow<Schedule?> = userDetailFeature.currentGroup
        .flatMapLatest { group ->
            group?.let {
                scheduleFeature.scheduleForGroup(it)
            } ?: flowOf(null)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val nextDayWithLessons: StateFlow<Day?> = combine(
        schedule,
        currentDateFlow,
        currentTime
    ) { currentSchedule, currentDate, currentTime ->
        if (currentSchedule != null && currentDate != null && currentTime != null) {
            currentSchedule.findNextDayWithLessonsAfter(date = currentDate, time = currentTime)
        } else {
            null
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )


    val weekNumber = scheduleFeature.weekNumber

    val weekLabel = scheduleFeature.weekLabel

    fun updateWeekNumber(){
        viewModelScope.launch {
            scheduleFeature.updateWeekNumber()
        }
    }

    fun updateScheduleForGroup(group: String) {
        viewModelScope.launch {
            scheduleFeature.updateScheduleForGroup(group)
        }
    }

    fun addGroupToHistory(group: String){
        viewModelScope.launch {
            NETICore.userDetailFeature.addGroupToHistory(group)
        }
    }

    fun removeGroupFromHistory(group: String){
        viewModelScope.launch {
            NETICore.userDetailFeature.removeGroupFromHistory(group)
        }
    }

    fun setCurrentGroup(group: String){
        viewModelScope.launch {
            NETICore.userDetailFeature.setCurrentGroup(group)
        }
    }

    fun updateWeekLabel(){
        viewModelScope.launch {
            scheduleFeature.updateWeekLabel()
        }
    }


    fun observeScheduleAndUpdateWidget(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            currentGroup
                .filter { !it.isNullOrEmpty() }
                .flatMapLatest { group -> scheduleFeature.scheduleForGroup(group.orEmpty()) }
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
}