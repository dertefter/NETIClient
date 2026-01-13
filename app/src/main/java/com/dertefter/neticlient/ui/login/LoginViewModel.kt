package com.dertefter.neticlient.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.features.authorization.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
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

    val savedUsersList = authorizationFeature.savedUsersList

    val loginScreenState = MutableStateFlow<LoginScreenState>(LoginScreenState.ADD_NEW_ACCOUNT)



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

    fun login(login: String, password: String, clearIfError: Boolean = false){
        viewModelScope.launch {
            authorizationFeature.login(
                User(
                    login,
                    password
                ),
                clearIfError
            )
        }
    }

    fun logout(){
        viewModelScope.launch {
            authorizationFeature.logout()
        }
    }

    fun removeUser(login: String){
        viewModelScope.launch {
            authorizationFeature.removeUserFromList(login)
        }
    }







}