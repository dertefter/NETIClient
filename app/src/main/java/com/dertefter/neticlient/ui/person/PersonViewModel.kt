package com.dertefter.neticlient.ui.person

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.features.person_detail.model.Person
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class PersonViewModel @Inject constructor(
    val netiCore: NETICore
) : ViewModel() {


    val personDetailFeature = netiCore.personDetailFeature

    fun personById(personId: String): Flow<Person?> {
        return personDetailFeature.personById(personId)
    }

    fun updatePersonById(personId: String) {
        viewModelScope.launch {
            personDetailFeature.updatePersonById(personId)
        }
    }

}