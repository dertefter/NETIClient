package com.dertefter.neticore.network.api

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface BaseApiService {

    @GET("studies/schedule/schedule_classes/schedule")
    suspend fun fetchWeekNumberList(@Query("group") group: String, @Query("week") week: String = "1"): Response<ResponseBody>

    @GET(".")
    suspend fun fetchWeekLabel(): Response<ResponseBody>

    @GET(".")
    suspend fun fetchWeekNumber(): Response<ResponseBody>

    @GET("studies/schedule/schedule_classes/schedule")
    suspend fun fetchScheduleForGuest(@Query("group") group: String, @Query("week") week: String = "1", @Query("print") print: String = "true"): Response<ResponseBody>





}