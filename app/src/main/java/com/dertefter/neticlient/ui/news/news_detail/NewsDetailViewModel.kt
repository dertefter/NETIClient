package com.dertefter.neticlient.ui.news.news_detail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.data.repository.CourcesRepository
import com.dertefter.neticlient.data.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsDetailViewModel @Inject constructor(
    private val newsRepository: NewsRepository
): ViewModel() {

    val newsDetailLiveData = MutableLiveData<ResponseResult>()

    fun fetchNewsDetail(id: String){
        viewModelScope.launch {
            newsDetailLiveData.postValue(ResponseResult(ResponseType.LOADING))
            val responseResult = newsRepository.fetchDetailNews(id)
            newsDetailLiveData.postValue(responseResult)
        }
    }

}