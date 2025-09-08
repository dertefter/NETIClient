package com.dertefter.neticore.network.api

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface DispaceApiService {


    @GET("user/proceed")
    suspend fun authDispace(@Query("login") login: String = "keycloak", @Query("password") password: String = "auth"): Response<ResponseBody>

}