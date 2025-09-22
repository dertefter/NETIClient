package com.dertefter.neticlient.ui.money.money_year

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.repository.MoneyRepository
import com.dertefter.neticore.NETICore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoneyItemsViewModel @Inject constructor(
    val netiCore: NETICore
): ViewModel() {

    val moneyFeature = netiCore.moneyFeature

    fun moneyItemsForYear(s: String) = moneyFeature.getMoneyItemsForYear(s).stateIn(
        viewModelScope,
        kotlinx.coroutines.flow.SharingStarted.Eagerly,
        null
    )

    fun updateMoneyItemsForYear(year: String){
        viewModelScope.launch {
            moneyFeature.updateMoneyItemsForYear(year)
        }
    }

}