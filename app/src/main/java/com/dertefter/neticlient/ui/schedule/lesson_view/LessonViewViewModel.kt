package com.dertefter.neticlient.ui.schedule.lesson_view

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticlient.data.model.CurrentTimeObject
import com.dertefter.neticlient.data.model.schedule.Lesson
import com.dertefter.neticlient.data.model.schedule.Time
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.data.repository.ScheduleRepository
import com.dertefter.neticlient.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.getOrPut

@HiltViewModel
class LessonViewViewModel @Inject constructor(): ViewModel() {

    val lessonLiveData = MutableLiveData<Lesson?>()
    val timeLiveData = MutableLiveData<Time?>()

    fun setData(lesson: Lesson, time: Time){
        viewModelScope.launch {
            lessonLiveData.postValue(lesson)
            timeLiveData.postValue(time)
        }
    }

    fun clearData() {
        lessonLiveData.postValue(null)
        timeLiveData.postValue(null)
    }

}