package com.dertefter.neticlient.data.network

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
import retrofit2.http.Streaming

interface ApiService {

    //авторизация

    @POST("ssoservice/json/authenticate")
    suspend fun auth(@Body requestBody: RequestBody): Response<ResponseBody>

    @FormUrlEncoded
    @POST("ssoservice/json/authenticate")
    suspend fun requestAuthId(@FieldMap params: Map<String, String> = mapOf("goto" to "https%3A%2F%2Fciu.nstu.ru%2Fstudent_study%2F")): Response<ResponseBody>


    @GET("studies/schedule/schedule_session/schedule")
    suspend fun getSessiaSchedule(@Query("group") group: String): Response<ResponseBody>

    @GET("student_study/personal/contact_info/")
    suspend fun getUserInfo(): Response<ResponseBody>

    @Streaming
    @GET("student_study/personal/photo_pass/")
    suspend fun getProfilePic(@Query("what") what: String = "5"): Response<ResponseBody>

    @GET("studies/schedule/schedule_classes/schedule")
    suspend fun getWeekNumberList(@Query("group") group: String, @Query("week") week: String = "1"): Response<ResponseBody>

    @GET("studies/schedule/schedule_classes/schedule")
    suspend fun getSchedule(@Query("group") group: String, @Query("print") print: String = "true"): Response<ResponseBody>

    @GET("student_study/timetable/timetable_lessons")
    suspend fun getIndividualSchedule(@Query("print") print: String = "true"): Response<ResponseBody>


    @GET("studies/schedule/schedule_classes")
    suspend fun getGroupList(@Query("query") group: String): Response<ResponseBody>

    @GET(".")
    suspend fun getCurrentWeekNumber(): Response<ResponseBody>

    @GET(".")
    suspend fun getHeaderLabel(): Response<ResponseBody>

    @GET("student_study/mess_teacher")
    suspend fun getMessages(@Query("year") year: String? = "-1"): Response<ResponseBody>

    @GET("didesk/index")
    @FormUrlEncoded
    suspend fun getCoursesSearchResult(query: String): Response<ResponseBody>

    @GET("user/proceed")
    suspend fun authDispace(@Query("login") login: String = "openam", @Query("password") password: String = "auth"): Response<ResponseBody>

    @FormUrlEncoded
    @POST("diclass/privmsg/dialog/")
    suspend fun getDispaceSenderList(@FieldMap params: HashMap<String?, String?>): Response<ResponseBody>

    @GET("/kaf/persons/{id}")
    suspend fun getPersonById(@Path("id") id: String): Response<ResponseBody>

    @GET(".")
    suspend fun getNews(@Query("main_events") page: String?): Response<ResponseBody>

    @GET("news/news_more")
    suspend fun getNewsDetail(@Query("idnews") id: String?): Response<ResponseBody>

    @GET("student_study/mess_teacher/view")
    suspend fun getMessageDetail(@Query("id") id: String?): Response<ResponseBody>

    @GET("student_study/student_info/progress")
    suspend fun getSessiaResults(): Response<ResponseBody>

    @GET("student_study/personal/money")
    suspend fun getMoneyYearsList(): Response<ResponseBody>

    @FormUrlEncoded
    @POST("student_study/personal/money")
    suspend fun getMoneyItems(@FieldMap params: HashMap<String?, String?>): Response<ResponseBody>

    @GET("student_study/personal/contact_info")
    suspend fun getProfileDetail(): Response<ResponseBody>

    @FormUrlEncoded
    @POST("student_study/personal/contact_info")
    suspend fun saveProfileDetails(@FieldMap params: HashMap<String?, String?>): Response<ResponseBody>

    @GET(".")
    suspend fun getPromoList(): Response<ResponseBody>

    @GET("phone/object")
    suspend fun findPerson(@Query("search_term") search_term: String?): Response<ResponseBody>

    @GET("student_study/docs/claims")
    suspend fun getDocuments(): Response<ResponseBody>


    @FormUrlEncoded
    @POST("student_study/docs/claims/ajax_claims")
    suspend fun getDocumentRequestItem(@FieldMap params: HashMap<String?, String?>): Response<ResponseBody>

    @FormUrlEncoded
    @POST("student_study/docs/claims")
    suspend fun claimNewDocument(@FieldMap params: HashMap<String?, String?>): Response<ResponseBody>

    @GET("student_study/docs/claims/edit_claim")
    suspend fun checkDocCancelable(@Query("id") id: String?): Response<ResponseBody>

    @FormUrlEncoded
    @POST("student_study/docs/claims/edit_claim")
    suspend fun cancelDocument(@Query("id") id: String?, @FieldMap params: HashMap<String?, String?>): Response<ResponseBody>


    //NEW AUTH
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

    @GET("student_study")
    suspend fun base(): Response<ResponseBody>
}