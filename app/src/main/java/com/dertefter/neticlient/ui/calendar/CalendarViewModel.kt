package com.dertefter.neticlient.ui.calendar

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticlient.data.model.calendar.CalendarEvent
import com.dertefter.neticlient.data.model.documents.DocumentRequestItem
import com.dertefter.neticlient.data.model.news.NewsItem
import com.dertefter.neticlient.data.model.news.NewsResponse
import com.dertefter.neticlient.data.model.news.PromoItem
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.data.repository.CalendarRepository
import com.dertefter.neticlient.data.repository.CourcesRepository
import com.dertefter.neticlient.data.repository.DocumentsRepository
import com.dertefter.neticlient.data.repository.NewsRepository
import com.dertefter.neticlient.ui.documents.DocumentsUiState
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