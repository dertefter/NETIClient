package com.dertefter.neticlient.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.features.authorization.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    val netiCore: NETICore
) : ViewModel(){

    val authorizationFeature = netiCore.authorizationFeature
    val userDetailFeature = netiCore.userDetailFeature


    val currentUser = authorizationFeature.currentUser
    val authStatus = authorizationFeature.ciuStatus

    val mobileAuthStatus = authorizationFeature.mobileStatus

    val userDetail = userDetailFeature.userDetail

    val userDetailMobile = userDetailFeature.userDetailMobile

    val userDetailStatus = userDetailFeature.status


    init {
        tryAuthorize()
    }


    fun tryAuthorize(){
        viewModelScope.launch {
            val currentUser = currentUser.first()
            if (currentUser != null){
                authorizationFeature.login(currentUser)
            } else {
                authorizationFeature.logout()
            }
        }
    }

    fun updateUserDetail(){
        viewModelScope.launch {
            userDetailFeature.updateUserDetail()
        }
    }

    fun login(login: String, password: String){
        viewModelScope.launch {
            authorizationFeature.login(
                User(
                    login,
                    password
                )
            )
        }
    }

    fun logout(){
        viewModelScope.launch {
            authorizationFeature.logout()
        }

    }







}