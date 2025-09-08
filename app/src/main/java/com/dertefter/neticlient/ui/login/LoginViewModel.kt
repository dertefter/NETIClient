package com.dertefter.neticlient.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticlient.data.model.AuthState
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.features.authorization.model.User
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {


    val authorizationFeature = NETICore.authorizationFeature
    val userDetailFeature = NETICore.userDetailFeature


    val currentUser = authorizationFeature.currentUser
    val authStatus = authorizationFeature.ciuStatus

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







}