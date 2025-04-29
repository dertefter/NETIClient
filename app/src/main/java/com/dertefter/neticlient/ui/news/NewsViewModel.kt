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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val newsRepository: NewsRepository
): ViewModel() {

    val newsListLiveData = MutableLiveData<List<NewsItem>?>()

    val promoListLiveData = MutableLiveData<ResponseResult>()

    val appBarExpanded = MutableLiveData<Boolean>()

    var page = 1

    var loading = false

    fun fetchNews(){
        if (loading) return
        viewModelScope.launch {
            Log.e("newsLoad", "loading")
            loading = true
            val responseResult = newsRepository.fetchNews(page)
            if (responseResult.responseType == ResponseType.SUCCESS && responseResult.data!= null){
                val lastValue = newsListLiveData.value?.toMutableList()
                val newsResponse = responseResult.data as NewsResponse
                if (!lastValue.isNullOrEmpty()) {
                    for (i in newsResponse.items){
                        if (!lastValue.contains(i)){
                            lastValue.add(i)
                        }
                    }
                    newsListLiveData.postValue(lastValue)
                    page++
                } else {
                    newsListLiveData.postValue(newsResponse.items)
                    page++
                }

            }
            loading = false

        }
    }

    fun fetchPromoList(){
        viewModelScope.launch {
            val responseResult = newsRepository.fetchPromoList()
            promoListLiveData.postValue(responseResult)
        }
    }

}