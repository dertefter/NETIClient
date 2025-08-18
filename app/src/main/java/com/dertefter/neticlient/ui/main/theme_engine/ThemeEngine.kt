package com.dertefter.neticlient.ui.main.theme_engine

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Base64
import androidx.core.content.edit
import java.io.ByteArrayOutputStream

object ThemeEngine {

    private var sharedPreferences: SharedPreferences? = null

    fun setup(context: Context) {
        sharedPreferences = context.getSharedPreferences("com.dertefter.neticlient.theme_engine", MODE_PRIVATE)
    }

    fun getSelectedColor(): Int {
        return sharedPreferences!!.getInt(
            "SELECTED_COLOR", Color.GREEN.toInt()
        )
    }

    fun setSelectedColor(color: Int?) {
        if (color == null){
            sharedPreferences?.edit {
                putInt("SELECTED_COLOR", Color.GREEN.toInt())
                apply()
            }
        }else{
            sharedPreferences?.edit {
                putInt("SELECTED_COLOR", color)
                apply()
            }
        }

    }

    fun getThemeType(): Int {

        //0 - STANDARD
        //1 - COLORED
        //2 - DYNAMIC

        return sharedPreferences!!.getInt(
            "THEME_TYPE", 0
        )
    }

    fun setThemeType(type: Int) {
        sharedPreferences?.edit {
            putInt("THEME_TYPE", type)
            apply()
        }

    }



}
