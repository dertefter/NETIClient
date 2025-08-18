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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository,
): ViewModel() {

    val userFlow = userRepository.getUserFlow()
    val authStateFlow = MutableStateFlow<AuthState>(
        value = AuthState.AUTHORIZING
    )

    init {
        tryAuthorize()
    }

    fun tryAuthorize(){
        viewModelScope.launch {
            val user = userRepository.getUserFlow().first()
            if (user != null){
                auth(user.login, user.password)
            } else {
                authStateFlow.value = AuthState.UNAUTHORIZED
            }
        }
    }


    fun auth(login: String, password: String){
        viewModelScope.launch {
            authStateFlow.value = AuthState.AUTHORIZING
            authStateFlow.value = userRepository.fetchAuth(login, password)
        }
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.setUser(null)
            authStateFlow.value = AuthState.UNAUTHORIZED
        }
    }


}