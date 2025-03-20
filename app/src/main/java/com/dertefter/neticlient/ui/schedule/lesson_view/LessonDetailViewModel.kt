package com.dertefter.neticlient.ui.schedule.lesson_view

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticlient.data.model.schedule.Lesson
import com.dertefter.neticlient.data.model.schedule.LessonDetail
import com.dertefter.neticlient.data.model.schedule.Time
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LessonDetailViewModel @Inject constructor(): ViewModel() {

    val lessonDetailLiveData = MutableLiveData<LessonDetail?>()

    fun setData(lessonDetail: LessonDetail){
        viewModelScope.launch {
            lessonDetailLiveData.postValue(lessonDetail)
        }
    }

    fun clearData() {
        lessonDetailLiveData.postValue(null)
    }

}