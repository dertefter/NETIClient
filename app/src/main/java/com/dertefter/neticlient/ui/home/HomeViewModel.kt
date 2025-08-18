package com.dertefter.neticlient.ui.home

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticlient.data.model.documents.DocumentRequestItem
import com.dertefter.neticlient.data.model.news.NewsItem
import com.dertefter.neticlient.data.model.news.NewsResponse
import com.dertefter.neticlient.data.model.news.PromoItem
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.data.repository.CourcesRepository
import com.dertefter.neticlient.data.repository.DocumentsRepository
import com.dertefter.neticlient.data.repository.NewsRepository
import com.dertefter.neticlient.ui.documents.DocumentsUiState
import com.dertefter.neticlient.ui.schedule.ScheduleUiState
import com.dertefter.neticlient.ui.sessia_schedule.ScheduleSessiaUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
): ViewModel() {

    var appVarIsLifted = false


}