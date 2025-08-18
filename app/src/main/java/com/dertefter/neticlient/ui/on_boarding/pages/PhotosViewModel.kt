package com.dertefter.neticlient.ui.on_boarding.pages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.profile_menu.ProfileMenuItem
import com.dertefter.neticlient.data.repository.RandomPhotoRepository
import com.dertefter.neticlient.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhotosViewModel @Inject constructor(
    private val randomPhotoRepository: RandomPhotoRepository
): ViewModel() {

    val photosFlow = randomPhotoRepository.getPhotoListFlow()

    fun updatePhotos(){
        viewModelScope.launch {
            randomPhotoRepository.updatePhotoList()
        }
    }

}