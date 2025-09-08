package com.dertefter.neticlient.ui.on_boarding.pages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticlient.data.repository.RandomPhotoRepository
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