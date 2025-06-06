package com.dertefter.neticlient.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.profile_menu.ProfileMenuItem
import com.dertefter.neticlient.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
): ViewModel() {

    private val _menuItems = MutableLiveData<List<ProfileMenuItem>>()
    val menuItems: LiveData<List<ProfileMenuItem>> = _menuItems

    val menuItemsAuthed: List<ProfileMenuItem> = listOf(
        ProfileMenuItem("Результаты сессии", R.drawable.school_icon, R.id.sessiaResultsFragment),
        ProfileMenuItem("Стипендии и выплаты", R.drawable.currency_ruble_icon, R.id.moneyFragment),
        ProfileMenuItem("Поиск сотрудников", R.drawable.person_search_icon, R.id.personSearchFragment),
        ProfileMenuItem("Заявки на документы", R.drawable.document_icon, R.id.documentsFragment),
        )
    val menuItemsUnauthed: List<ProfileMenuItem> = listOf(
        ProfileMenuItem("Результаты сессии", R.drawable.school_icon, null, false),
        ProfileMenuItem("Стипендии и выплаты", R.drawable.currency_ruble_icon, null, false),
        ProfileMenuItem("Поиск сотрудников", R.drawable.person_search_icon, R.id.personSearchFragment, false),
        ProfileMenuItem("Заявки на документы", R.drawable.document_icon, R.id.documentsFragment, false),
    )

    fun updateMenuItems(isAuthed: Boolean){
        if (isAuthed){
            _menuItems.value = menuItemsAuthed
        } else {
            _menuItems.value = menuItemsUnauthed
        }
    }

}