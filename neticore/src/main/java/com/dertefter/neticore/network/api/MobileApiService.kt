package com.dertefter.neticore.network.api

import com.dertefter.neticore.features.inbox.model.Message
import com.dertefter.neticore.features.user_detail.model.UserDetail
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface MobileApiService {


    @PUT("student/modify_data/app/update_message_read_status")
    suspend fun updateMessageReadStatus(
        @Body body: Map<String, Int>
    ): Response<ResponseBody>

    @POST("token/new-auth")
    suspend fun newAuth(@Query("disableNotification") disableNotification: Boolean = true,
                        @Body params: HashMap<String?, String?>): Response<ResponseBody>

    @GET("student/get_data/app/get_user_info")
    suspend fun fetchUserDetail(): Response<List<UserDetail>>


    //https://api.ciu.nstu.ru/v1.1/student/get_data/app/get_messages?start_date=1990-01-01T00:00:00.000Z

    @GET("student/get_data/app/get_messages")
    suspend fun fetchMessages(@Query("start_date") start_date: String = "1990-01-01T00:00:00.000Z"): Response<List<Message>>




}