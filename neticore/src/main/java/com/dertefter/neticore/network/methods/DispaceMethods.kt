package com.dertefter.neticore.network.methods

import android.util.Log
import com.dertefter.neticore.network.api.DispaceApiService
import com.dertefter.neticore.network.api.Login2ApiService
import com.dertefter.neticore.network.parser.HtmlParserAuthorization

class DispaceMethods(val api: DispaceApiService) {

    suspend fun authUserDispace(): Boolean {
        try {
            val response = api.authDispace()
            if (response.isSuccessful){
                return true
            }
            return false
        } catch (e: Exception) {
            return false
        }
    }

}