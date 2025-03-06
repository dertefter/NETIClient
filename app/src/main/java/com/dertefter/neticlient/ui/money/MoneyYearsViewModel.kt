package com.dertefter.neticlient.ui.money

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.repository.MoneyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoneyYearsViewModel @Inject constructor(
    private val moneyRepository: MoneyRepository
): ViewModel() {

    val yearListLiveData = MutableLiveData<ResponseResult>()

    fun fetchYearList(){
        viewModelScope.launch {
            yearListLiveData.postValue(moneyRepository.fetchYears())
        }
    }

}