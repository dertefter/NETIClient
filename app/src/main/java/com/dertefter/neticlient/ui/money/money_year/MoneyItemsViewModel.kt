package com.dertefter.neticlient.ui.money.money_year

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.repository.MoneyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoneyItemsViewModel @Inject constructor(
    private val moneyRepository: MoneyRepository
): ViewModel() {

    val moneyItemsLiveData = MutableLiveData<ResponseResult>()

    fun fetchMoneyItemsForYear(year: String){
        viewModelScope.launch {
            moneyItemsLiveData.postValue(moneyRepository.fetchMoneyItems(year))
        }
    }

}