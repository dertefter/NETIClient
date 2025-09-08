package com.dertefter.neticlient.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticore.NETICore
import kotlinx.coroutines.launch


class ProfileViewModel (): ViewModel() {

    val lksList = NETICore.userDetailFeature.lksList

    fun updateLks(){
        viewModelScope.launch {
            NETICore.userDetailFeature.updateLksList()
        }
    }

    fun updateUserDetail(){
        viewModelScope.launch {
            NETICore.userDetailFeature.updateUserDetail()
        }
    }



    fun setLksById(id: Int){
        viewModelScope.launch {
            NETICore.userDetailFeature.setLksById(id)
        }
    }

}