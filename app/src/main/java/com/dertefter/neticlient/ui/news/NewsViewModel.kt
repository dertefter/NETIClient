package com.dertefter.neticlient.ui.news

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticlient.data.model.news.NewsItem
import com.dertefter.neticlient.data.model.news.NewsResponse
import com.dertefter.neticlient.data.model.news.PromoItem
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.data.repository.CourcesRepository
import com.dertefter.neticlient.data.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val newsRepository: NewsRepository
): ViewModel() {

    val newsListFlow = MutableStateFlow<List<NewsItem>?>(emptyList())

    val isNewsUpdateFlow = MutableStateFlow<Boolean>(false)

    val promoListFlow = newsRepository.getPromoListFlow()

    var page = 1

    fun fetchNews(withClear: Boolean = false){

        viewModelScope.launch {
            if (isNewsUpdateFlow.value && !withClear) return@launch
            isNewsUpdateFlow.value = true

            if (withClear){
                newsListFlow.value = emptyList()
                page = 1
            }

            val responseResult = newsRepository.fetchNews(page)
            if (responseResult.responseType == ResponseType.SUCCESS && responseResult.data!= null){
                val lastValue = newsListFlow.value?.toMutableList()
                val newsResponse = responseResult.data as NewsResponse
                if (!lastValue.isNullOrEmpty()) {
                    for (i in newsResponse.items){
                        if (!lastValue.contains(i)){
                            lastValue.add(i)
                        }
                    }
                    newsListFlow.value = lastValue
                    page++
                } else {
                    newsListFlow.value = newsResponse.items
                    page++
                }

            }
            isNewsUpdateFlow.value = false

        }
    }

    fun updatePromoList(){
        viewModelScope.launch {
            newsRepository.updatePromoList()
        }
    }



}