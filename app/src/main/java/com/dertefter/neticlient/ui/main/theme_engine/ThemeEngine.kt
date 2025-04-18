package com.dertefter.neticlient.ui.main.theme_engine

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.core.content.edit
import com.dertefter.neticlient.R

object ThemeEngine {

    private var sharedPreferences: SharedPreferences? = null
    fun setup(context: Context) {
        sharedPreferences = context.getSharedPreferences("com.dertefter.neticlient.theme_engine", MODE_PRIVATE)
    }

    fun getSelectedTheme(): Int {
        return sharedPreferences!!.getInt(
                "SELECTED_THEME",
        R.style.GreenTheme
        )
    }

    fun setSelectedTheme(theme: Int) {
        sharedPreferences?.edit {
            putInt("SELECTED_THEME", theme)
            apply()
        }
    }

}