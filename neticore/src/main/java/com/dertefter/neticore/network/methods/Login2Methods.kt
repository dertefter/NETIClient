package com.dertefter.neticore.network.methods

import android.util.Log
import com.dertefter.neticore.network.api.Login2ApiService
import com.dertefter.neticore.network.parser.HtmlParserAuthorization

class Login2Methods(val api: Login2ApiService) {

    suspend fun authUser(login: String, password: String): Boolean? {
        try {
            Log.e("authUser", "$login $password")
            val response1 = api.tst1()
            val respBody1 = response1.body()?.string().toString()
            val params = HtmlParserAuthorization().extractFormParams(respBody1)
            val paramLoginPassword = HashMap<String?, String?>()
            paramLoginPassword["username"] = login
            paramLoginPassword["selected_subset"] = ""
            paramLoginPassword["username-visible"] = login
            paramLoginPassword["password"] = password
            paramLoginPassword["credentialId"] = ""
            Log.e("params", params.toString())
            val resp2 = api.tst2(
                session_code = params["session_code"]!!,
                execution = params["execution"]!!,
                client_id = params["client_id"]!!,
                tab_id = params["tab_id"]!!,
                client_data = params["client_data"]!!,
                params = paramLoginPassword
            )
            return true

        } catch (e: Exception) {
            Log.e("authUserrrr", e.stackTraceToString())
            return false
        }
    }

}