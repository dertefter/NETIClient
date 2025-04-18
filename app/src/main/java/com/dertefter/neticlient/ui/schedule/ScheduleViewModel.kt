package com.dertefter.neticlient.ui.schedule

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticlient.data.model.CurrentTimeObject
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.data.repository.ScheduleRepository
import com.dertefter.neticlient.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.getOrPut

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val userRepository: UserRepository
): ViewModel() {

    val groupHistoryLiveData = MutableLiveData<List<String>>()

    val selectedGroupLiveData = MutableLiveData<String?>()

    private val scheduleLiveDataMap: MutableMap<String, MutableLiveData<ResponseResult>> = mutableMapOf()

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

    fun setSelectedGroup(group: String){
        viewModelScope.launch {
            userRepository.satSelectedGroup(group)
            addGroupToHistory(group)
            getSelectedGroup()
        }
    }

    fun getSelectedGroup(){
        viewModelScope.launch {
            val group = userRepository.getSelectedGroup().first()
            selectedGroupLiveData.postValue(group)
        }
    }

    fun getScheduleLiveData(group: String): MutableLiveData<ResponseResult> {
        return scheduleLiveDataMap.getOrPut(group) {
            MutableLiveData<ResponseResult>()
        }
    }

    fun fetchSchedule(group: String){
        viewModelScope.launch {
            val scheduleLiveData = getScheduleLiveData(group)
            scheduleLiveData.postValue(ResponseResult(ResponseType.LOADING))
            val schedule = if (
                userRepository.getUser().first() != null && userRepository.getUser().first()!!.group == group
            ){
                scheduleRepository.fetchSchedule(group, true)
            } else {
                scheduleRepository.fetchSchedule(group)
            }
            if (schedule != null){
                scheduleLiveData.postValue(ResponseResult(ResponseType.SUCCESS, data = schedule))
            } else {
                Log.e("getting local schedudk", "bfbfbf")
                getLocalSchedule(group)
            }
        }
    }

    private fun getLocalSchedule(group: String){
        viewModelScope.launch {
            val scheduleLiveData = getScheduleLiveData(group)
            scheduleLiveData.postValue(ResponseResult(ResponseType.LOADING))
            val schedule = scheduleRepository.getLocalSchedule(group).first()
            if (schedule != null){
                scheduleLiveData.postValue(ResponseResult(ResponseType.SUCCESS, data = schedule))
            } else {
                scheduleLiveData.postValue(ResponseResult(ResponseType.ERROR))
            }
        }
    }

    fun fetchCurrentWeekNumber(){
        viewModelScope.launch {
            val weekNumber = scheduleRepository.fetchCurrentWeekNumber()
            CurrentTimeObject.currentWeekLiveData.postValue(weekNumber)
        }


    }


}