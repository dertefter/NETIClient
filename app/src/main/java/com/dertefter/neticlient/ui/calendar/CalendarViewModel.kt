package com.dertefter.neticlient.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticlient.data.model.calendar.CalendarEvent
import com.dertefter.neticlient.data.repository.CalendarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository
): ViewModel() {

    fun getEventsFlowForMonth(year: String, month: String): Flow<List<CalendarEvent>?> {
        return calendarRepository.getEventsFlowForMonth(year, month)
    }

    fun updateEventsForMonth(year: String, month: String,){
        viewModelScope.launch {
            calendarRepository.updateEventsForMonth(year, month)
        }
    }
}