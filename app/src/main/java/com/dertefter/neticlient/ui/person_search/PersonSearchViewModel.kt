package com.dertefter.neticlient.ui.person_search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.data.repository.PersonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonSearchViewModel @Inject constructor(
    private val personRepository: PersonRepository,
) : ViewModel() {
    val personIdListLiveData = MutableLiveData<ResponseResult>()


    fun fetchPersonSearchResults(q: String) {
        viewModelScope.launch {
            personIdListLiveData.postValue(ResponseResult(ResponseType.LOADING))
            val personIdList = personRepository.fetchPersonSearchResults(q)
            personIdListLiveData.postValue(personIdList)
        }
    }

}