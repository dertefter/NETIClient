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

interface CiuApiService {

    @GET("student_study/personal/contact_info/")
    suspend fun fetchUserDetail(): Response<ResponseBody>

    @GET("student_study/student_info/progress")
    suspend fun fetchSessiaResults(): Response<ResponseBody>


    @FormUrlEncoded
    @POST("student_study/personal/contact_info/")
    suspend fun setLksById(@FieldMap params: HashMap<String?, Int?>): Response<ResponseBody>


    @GET("/kaf/persons/{id}")
    suspend fun fetchPersonById(@Path("id") id: String): Response<ResponseBody>


    @GET("student_study/timetable/timetable_lessons")
    suspend fun fetchSchedule(@Query("print") print: String = "true"): Response<ResponseBody>

}