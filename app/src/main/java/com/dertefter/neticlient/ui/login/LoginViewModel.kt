package com.dertefter.neticlient.ui.login

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticlient.data.model.AuthState
import com.dertefter.neticlient.data.model.User
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository,
): ViewModel() {

    val userLiveData = MutableLiveData<User?>()
    val authStateLiveData = MutableLiveData<AuthState>()

    init {
        tryAuthorize()
    }

    fun tryAuthorize(){
        viewModelScope.launch {
            val user = userRepository.getUser().first()
            if (user != null){
                authStateLiveData.postValue(AuthState.AUTHORIZED)
                userLiveData.postValue(user)
                auth(user.login, user.password)
            } else {
                authStateLiveData.postValue(AuthState.UNAUTHORIZED)
            }
        }
    }


    fun auth(login: String, password: String){
        viewModelScope.launch {
            authStateLiveData.postValue(AuthState.AUTHORIZING)
            val responseResult = userRepository.fetchAuth(login, password)
            if (responseResult.responseType == ResponseType.SUCCESS){
                val user = userRepository.getUser().first()
                authStateLiveData.postValue(AuthState.AUTHORIZED)
                userLiveData.postValue(user)
            } else {
                authStateLiveData.postValue(AuthState.AUTHORIZED_WITH_ERROR)
            }
        }
    }

    fun fetchUser(){
        viewModelScope.launch {
            val user = userRepository.getUser().first()
            userLiveData.postValue(user)
        }
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.removeUser()
            authStateLiveData.postValue(AuthState.UNAUTHORIZED)
            userLiveData.postValue(null)
        }
    }


}