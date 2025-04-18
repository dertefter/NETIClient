package com.dertefter.neticlient.ui.profile.profile_detail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileDetailViewModel @Inject constructor(
    private val userRepository: UserRepository
): ViewModel() {

    val profileDetailLiveData = MutableLiveData<ResponseResult>()

    fun fetchProfileDetails(){
        viewModelScope.launch {
            profileDetailLiveData.postValue(ResponseResult(ResponseType.LOADING))
            profileDetailLiveData.postValue(userRepository.fetchProfileDetail())
        }
    }

    fun saveData(
        n_email: String,
        n_address: String,
        n_phone: String,
        n_snils: String,
        n_oms: String,
        n_vk: String,
        n_tg: String,
        n_leader: String
    ) {
        viewModelScope.launch {
            profileDetailLiveData.postValue(ResponseResult(ResponseType.LOADING))
            profileDetailLiveData.postValue(userRepository.saveProfileDetails(n_email, n_address, n_phone, n_snils, n_oms, n_vk, n_tg, n_leader))
        }
    }
}