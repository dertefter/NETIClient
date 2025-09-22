package com.dertefter.neticlient.ui.money

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.repository.MoneyRepository
import com.dertefter.neticore.NETICore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoneyYearsViewModel @Inject constructor(
    val netiCore: NETICore
): ViewModel() {


    val moneyFeature = netiCore.moneyFeature

    val years = moneyFeature.yearList
    val status = moneyFeature.status

    fun updateYearList(){
        viewModelScope.launch {
            moneyFeature.updateYearList()
        }
    }

}