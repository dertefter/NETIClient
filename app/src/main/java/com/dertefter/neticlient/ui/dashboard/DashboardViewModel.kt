package com.dertefter.neticlient.ui.dashboard

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticlient.data.model.dashboard.DashboardItem
import com.dertefter.neticlient.data.model.dashboard.DashboardItemType
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository
): ViewModel() {

    val headerLabelLiveData = MutableLiveData<ResponseResult>()
    val dashboardItemListLiveData = MutableLiveData<List<DashboardItem>>()

    var newItemType: DashboardItemType? = null
    var newItemData: String? = null

    fun fetchHeaderLabel(){
        viewModelScope.launch {
            val responseResult = dashboardRepository.fetchHeaderLabel()
            headerLabelLiveData.postValue(responseResult)
        }
    }

    fun fetchDashboardItemList(){
        viewModelScope.launch {
            val responseResult = dashboardRepository.getDashboardItemList()
            dashboardItemListLiveData.postValue(responseResult.first())
        }
    }

    fun addDashboardItem(item: DashboardItem) {
        viewModelScope.launch {
            dashboardRepository.addDashboardItem(item)
            fetchDashboardItemList()
        }
    }

    fun removeItem(dashboardItem: DashboardItem) {
        viewModelScope.launch {
            dashboardRepository.removeItem(dashboardItem)
            fetchDashboardItemList()
        }
    }

    fun moveItem(fromPosition: Int, toPosition: Int) {
        viewModelScope.launch {
            dashboardRepository.moveItem(fromPosition, toPosition)
        }
    }


}