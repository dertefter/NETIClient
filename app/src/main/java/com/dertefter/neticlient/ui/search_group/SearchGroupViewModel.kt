package com.dertefter.neticlient.ui.search_group

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.repository.SearchGroupRepository
import com.dertefter.neticore.NETICore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchGroupViewModel @Inject constructor(
    private val searchGroupRepository: SearchGroupRepository,
    private val netiCore: NETICore
): ViewModel() {
    val userDetailFeature = netiCore.userDetailFeature
    val groupListLiveData = MutableLiveData<ResponseResult>()
    val groupHistory =  userDetailFeature.groupHistory

    fun fetchGroupList(group: String) {
        viewModelScope.launch {
            val responseResult = searchGroupRepository.fetchGroupList(group)
            groupListLiveData.postValue(responseResult)
        }
    }

    fun removeGroupFromHistory(group: String){
        viewModelScope.launch {
            netiCore.userDetailFeature.removeGroupFromHistory(group)
        }
    }

    fun addGroupToHistory(group: String){
        viewModelScope.launch {
            netiCore.userDetailFeature.addGroupToHistory(group)
        }
    }

    fun setCurrentGroup(group: String){
        viewModelScope.launch {
            netiCore.userDetailFeature.setCurrentGroup(group)
        }
    }


}