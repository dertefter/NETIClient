package com.dertefter.neticlient.ui.news

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticlient.data.model.news.PromoItem
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.repository.CourcesRepository
import com.dertefter.neticlient.data.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val newsRepository: NewsRepository
): ViewModel() {

    val newsResponseLiveData = MutableLiveData<ResponseResult>()

    val promoListLiveData = MutableLiveData<ResponseResult>()

    fun fetchNews(page: Int){
        Log.e("fetchNews", page.toString())
        viewModelScope.launch {
            val responseResult = newsRepository.fetchNews(page)
            newsResponseLiveData.postValue(responseResult)
        }
    }

    fun fetchPromoList(){
        viewModelScope.launch {
            val responseResult = newsRepository.fetchPromoList()
            promoListLiveData.postValue(responseResult)
        }
    }

}