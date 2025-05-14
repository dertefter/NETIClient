package com.dertefter.neticlient.ui.person

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.data.repository.MessagesRepository
import com.dertefter.neticlient.data.repository.PersonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonViewModel @Inject constructor(
    private val personRepository: PersonRepository,
) : ViewModel() {
    private val personsLiveDataMap = mutableMapOf<String, MutableLiveData<ResponseResult>>()

    fun getLiveDataForId(id: String): MutableLiveData<ResponseResult> {
        if (personsLiveDataMap.containsKey(id)){
            return personsLiveDataMap[id]!!
        } else {
            personsLiveDataMap[id] = MutableLiveData<ResponseResult>()
            return personsLiveDataMap[id]!!
        }
    }

    fun fetchPerson(id: String, forceOffline: Boolean = true) {
        val liveData = getLiveDataForId(id)
        viewModelScope.launch {
            liveData.postValue(ResponseResult(ResponseType.LOADING))
            val person = personRepository.fetchPersonById(id, forceOffline)
            liveData.postValue(person)
        }
    }

}