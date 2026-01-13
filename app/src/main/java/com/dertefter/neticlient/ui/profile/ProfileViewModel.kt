package com.dertefter.neticlient.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticore.NETICore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ProfileViewModel @Inject constructor(
    val netiCore: NETICore
) : ViewModel() {
    val studentGroup = netiCore.studentsFeature.studentGroup

    val lksList = netiCore.userDetailFeature.lksList



    fun updateLks(){
        viewModelScope.launch {
            netiCore.userDetailFeature.updateLksList()
        }
    }

    fun updateUserDetail(){
        viewModelScope.launch {
            netiCore.userDetailFeature.updateUserDetail()
        }
    }

    fun updateStudentGroup(id: Int?){
        viewModelScope.launch {
            netiCore.studentsFeature.updateStudentGroup(id)
        }
    }



    fun setLksById(id: Int){
        viewModelScope.launch {
            netiCore.userDetailFeature.setLksById(id)
        }
    }

}