package com.dertefter.neticlient.ui.profile.profile_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticore.NETICore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject


class ProfileDetailViewModel(): ViewModel() {


   val userDetail = NETICore.userDetailFeature.userDetail

    val status = NETICore.userDetailFeature.status


    fun updateUserDetail() {
        viewModelScope.launch {
            NETICore.userDetailFeature.updateUserDetail()
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
            // TODO
        }
    }
}