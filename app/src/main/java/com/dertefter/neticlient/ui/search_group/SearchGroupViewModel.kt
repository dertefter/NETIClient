package com.dertefter.neticlient.ui.search_group

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.repository.ScheduleRepository
import com.dertefter.neticlient.data.repository.SearchGroupRepository
import com.dertefter.neticlient.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchGroupViewModel @Inject constructor(
    private val searchGroupRepository: SearchGroupRepository,
    private val userRepository: UserRepository,
): ViewModel() {

    val groupListLiveData = MutableLiveData<ResponseResult>()
    val groupHistoryLiveData = MutableLiveData<List<String>>()

    fun fetchGroupList(group: String) {
        viewModelScope.launch {
            val responseResult = searchGroupRepository.fetchGroupList(group)
            groupListLiveData.postValue(responseResult)
        }
    }

    fun getGroupsHistory(){
        viewModelScope.launch {
            userRepository.getGroupHistory().collect{
                groupHistoryLiveData.postValue(it)
            }
        }
    }

    fun removeGroupFromHistory(group: String){
        viewModelScope.launch {
            userRepository.removeGroupFromHistory(group)
            getGroupsHistory()
        }
    }


}