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

interface Login2ApiService {
    @POST("ssoservice/json/authenticate")
    suspend fun auth(@Body requestBody: RequestBody): Response<ResponseBody>


    @GET("realms/master/protocol/openid-connect/auth")
    suspend fun tst1(
        @Query("response_type") response_type: String = "code",
        @Query("client_id") client_id: String = "ciu-web-id-client",
        @Query("redirect_uri") redirect_uri: String = "https://id.nstu.ru/account_recovery_data",
        @Query("scope") scope: String = "openid email profile",
    ): Response<ResponseBody>


    @FormUrlEncoded
    @POST("realms/master/login-actions/authenticate")
    suspend fun tst2(
        @Query("session_code") session_code: String = "code",
        @Query("execution") execution: String = "ciu-web-id-client",
        @Query("client_id") client_id: String = "https://id.nstu.ru/account_recovery_data",
        @Query("tab_id") tab_id: String = "tab_id",
        @Query("client_data") client_data: String = "client_data",
        @FieldMap params: HashMap<String?, String?>
    ): Response<ResponseBody>

}