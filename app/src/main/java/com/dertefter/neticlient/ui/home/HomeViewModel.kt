package com.dertefter.neticlient.ui.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class HomeViewModel() : ViewModel() {

    private val _appVarIsLifted = MutableStateFlow(false)
    val appVarIsLifted: StateFlow<Boolean> = _appVarIsLifted

    fun setAppVarIsLifted(value: Boolean) {
        _appVarIsLifted.value = value
    }

    private val _nowY = MutableStateFlow(0)
    val nowY: StateFlow<Int> = _nowY

    private val _futureY = MutableStateFlow(0)
    val futureY: StateFlow<Int> = _futureY

    private val _pastY = MutableStateFlow(0)
    val pastY: StateFlow<Int> = _pastY

    private val _scrollY = MutableStateFlow(0)
    val scrollY: StateFlow<Int> = _scrollY


    fun setNowY(value: Int) {
        _nowY.value = value
    }

    fun setPastY(value: Int) {
        _pastY.value = value
    }


    fun setFutureY(value: Int) {
        _futureY.value = value
    }

    fun setScrollY(value: Int) {
        _scrollY.value = value
        if (value == 0){
            setAppVarIsLifted(false)
        }else{
            setAppVarIsLifted(true)
        }
    }


}
