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
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.features.person_detail.model.Person
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject


class PersonViewModel() : ViewModel() {

    val personDetailFeature = NETICore.personDetailFeature

    fun personById(personId: String): Flow<Person?> {
        return personDetailFeature.personById(personId)
    }

    fun updatePersonById(personId: String) {
        viewModelScope.launch {
            personDetailFeature.updatePersonById(personId)
        }
    }

}