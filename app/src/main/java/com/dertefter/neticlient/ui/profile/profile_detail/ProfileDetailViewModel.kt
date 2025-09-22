package com.dertefter.neticlient.ui.profile.profile_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticore.NETICore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ProfileDetailViewModel @Inject constructor(
    val netiCore: NETICore
) : ViewModel() {


   val userDetail = netiCore.userDetailFeature.userDetail.stateIn(
       viewModelScope,
       initialValue = null,
       started = SharingStarted.Eagerly
   )

    val status = netiCore.userDetailFeature.status


    fun updateUserDetail() {
        viewModelScope.launch {
            netiCore.userDetailFeature.updateUserDetail()
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