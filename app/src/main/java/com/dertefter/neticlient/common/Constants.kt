package com.dertefter.neticlient.common

import androidx.datastore.preferences.core.stringPreferencesKey

object Constants {



    const val USER_PREFERENCE = "user_preference"

    val LOGIN = stringPreferencesKey("login")
    val PASSWORD = stringPreferencesKey("password")
    val NAME = stringPreferencesKey("name")
    val GROUP = stringPreferencesKey("group")
    val SELECTED_GROUP = stringPreferencesKey("selected_group")
    val  PROFILE_PIC_PATH = stringPreferencesKey("profile_pic_path")
    val HEADER_LABEL = stringPreferencesKey("header_label")
    val DASHBOARD_ITEMS = stringPreferencesKey("dashboard_items")
    val GROUP_HISTORY = stringPreferencesKey("group_history")
    val USER = stringPreferencesKey("user")
}