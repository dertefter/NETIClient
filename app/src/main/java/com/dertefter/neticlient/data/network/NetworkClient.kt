package com.dertefter.neticlient.data.network

import android.content.Context
import android.util.Log
import com.dertefter.neticlient.data.model.dispace.messages.Companion
import com.dertefter.neticlient.data.model.dispace.messages.CompanionList
import com.dertefter.neticlient.data.model.messages.MessageDetail
import com.dertefter.neticlient.data.model.money.MoneyItem
import com.dertefter.neticlient.data.model.news.NewsDetail
import com.dertefter.neticlient.data.model.news.NewsResponse
import com.dertefter.neticlient.data.model.person.Person
import com.dertefter.neticlient.data.model.profile_detail.ProfileDetail
import com.dertefter.neticlient.data.model.sessia_results.SessiaResultSemestr
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.data.network.model.auth.AuthResponse
import com.dertefter.neticlient.data.network.model.auth.ResponseFromAuthIdRequest
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class NetworkClient @Inject constructor(
    private val context: Context,
    private var client: OkHttpClient,
    private var dispaceClient: OkHttpClient,
    @Named("auth") private var authRetrofit: Retrofit,
    @Named("ciu") private var ciuRetrofit: Retrofit,
    @Named("base") private var baseRetrofit: Retrofit,
    @Named("dispace") private var dispaceRetrofit: Retrofit
) {
    var authApiService: ApiService = authRetrofit.create(ApiService::class.java)
    var ciuApiService: ApiService = ciuRetrofit.create(ApiService::class.java)
    var baseApiService: ApiService = baseRetrofit.create(ApiService::class.java)
    var dispaceApiService: ApiService = dispaceRetrofit.create(ApiService::class.java)

    fun rebuildClientWithToken(token: String?) {

        val cookieJar = object : CookieJar {
            val cookieStore: HashMap<String, List<Cookie>> = HashMap()
            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                cookieStore[url.host] = cookies
            }

            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                val cookies = cookieStore[url.host]
                return cookies ?: ArrayList()
            }
        }


        dispaceClient = OkHttpClient.Builder().cookieJar(cookieJar)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Cookie", "NstuSsoToken=$token")
                    .build()
                chain.proceed(request)
            }
            .build()

        client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Cookie", "NstuSsoToken=$token")
                    .build()
                chain.proceed(request)
            }
            .build()

        authRetrofit = authRetrofit.newBuilder().client(client).build()
        ciuRetrofit = ciuRetrofit.newBuilder().client(client).build()
        baseRetrofit = baseRetrofit.newBuilder().client(client).build()
        dispaceRetrofit = dispaceRetrofit.newBuilder().client(dispaceClient).build()

        authApiService = authRetrofit.create(ApiService::class.java)
        ciuApiService = ciuRetrofit.create(ApiService::class.java)
        baseApiService = baseRetrofit.create(ApiService::class.java)
        dispaceApiService = dispaceRetrofit.create(ApiService::class.java)
    }

    suspend fun getResponseFromAuthIdRequest(): ResponseFromAuthIdRequest? {
        return try {
            val response = authApiService.requestAuthId()
            if (response.isSuccessful) {
                Gson().fromJson(response.body()?.string(), ResponseFromAuthIdRequest::class.java)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun authUser(login: String, password: String): ResponseResult {
        return try {
            val authIdRequest = getResponseFromAuthIdRequest()
                ?: return ResponseResult(ResponseType.ERROR, "Ошибка сети или сервера")

            authIdRequest.callbacks.forEach { callback ->
                when (callback.type) {
                    "NameCallback" -> callback.input.find { it.name == "IDToken1" }?.value = login
                    "PasswordCallback" -> callback.input.find { it.name == "IDToken2" }?.value = password
                }
            }

            val requestBody = Gson().toJson(authIdRequest).toRequestBody("application/json".toMediaTypeOrNull())
            val response = authApiService.auth(requestBody)

            if (response.isSuccessful) {
                val authResponse = Gson().fromJson(response.body()?.string(), AuthResponse::class.java)
                authResponse.tokenId?.let {
                    rebuildClientWithToken(it)
                    ResponseResult(ResponseType.SUCCESS, "Успешный вход", data = it)
                } ?: ResponseResult(ResponseType.ERROR, "Неверные данные")
            } else {
                ResponseResult(ResponseType.ERROR, "Ошибка авторизации")
            }
        } catch (e: Exception) {
            ResponseResult(ResponseType.ERROR, "Ошибка сети: ${e.message}")
        }
    }

    suspend fun authUserDispace(token: String): Boolean {
        try {
            val response = dispaceApiService.authDispace()
            if (response.isSuccessful){
                return true
            }
            return false
        } catch (e: Exception) {
            return false
        }
    }

    suspend fun getProfilePic(): ResponseResult {
        return try {
            val response = ciuApiService.getProfilePic()
            if (response.isSuccessful) {
                val responseBody = response.body()
                val path = File(context.cacheDir, "profile_pic.jpg").absolutePath
                val file = File(path)
                withContext(Dispatchers.IO) {
                    responseBody?.byteStream()?.use { input ->
                        FileOutputStream(file).use { output ->
                            input.copyTo(output)
                        }
                    }
                }
                ResponseResult(ResponseType.SUCCESS, data = path)
            } else {
                ResponseResult(ResponseType.ERROR, "Ошибка загрузки изображения")
            }
        } catch (e: Exception) {
            ResponseResult(ResponseType.ERROR, "Ошибка: ${e.message}")
        }
    }

    suspend fun getSessiaSchedule(group: String): ResponseResult {
        return try {
            val response = baseApiService.getSessiaSchedule(group)
            if (response.isSuccessful) {
                val schedule = HtmlParser().parseSessiaSchedule(response.body())
                ResponseResult(ResponseType.SUCCESS, data = schedule)
            } else {
                ResponseResult(ResponseType.ERROR, "Ошибка получения расписания")
            }
        } catch (e: Exception) {
            ResponseResult(ResponseType.ERROR, "Ошибка: ${e.message}")
        }
    }

    suspend fun getUserInfo(): ResponseResult {
        return try {
            val response = ciuApiService.getUserInfo()
            if (response.isSuccessful) {
                val userInfo = HtmlParser().parseUserInfo(response.body())
                ResponseResult(ResponseType.SUCCESS, data = userInfo)
            } else {
                ResponseResult(ResponseType.ERROR, "Ошибка получения данных пользователя")
            }
        } catch (e: Exception) {
            ResponseResult(ResponseType.ERROR, "Ошибка: ${e.message}")
        }
    }

    suspend fun getWeekNumberList(group: String): ResponseResult {
        return try {
            val response = baseApiService.getWeekNumberList(group)
            if (response.isSuccessful) {
                val weekNumberList = HtmlParser().parseWeekNumberList(response.body())
                ResponseResult(ResponseType.SUCCESS, data = weekNumberList)
            } else {
                ResponseResult(ResponseType.ERROR, "Ошибка получения списка недель")
            }
        } catch (e: Exception) {
            ResponseResult(ResponseType.ERROR, "Ошибка: ${e.message}")
        }
    }

    suspend fun getSchedule(group: String, isIndividual: Boolean = false): ResponseResult {
        return try {
            val response = if (isIndividual){
                Log.e("aboba", "indi")
                ciuApiService.getIndividualSchedule()
            }else{
                baseApiService.getSchedule(group)
            }

            if (response.isSuccessful) {
                val schedule = HtmlParser().parseSchedule(response.body())
                ResponseResult(ResponseType.SUCCESS, data = schedule)
            } else {
                ResponseResult(ResponseType.ERROR, "Ошибка получения расписания")
            }
        } catch (e: Exception) {
            ResponseResult(ResponseType.ERROR, "Ошибка: ${e.message}")
        }
    }

    suspend fun getGroupList(group: String): ResponseResult {
        return try {
            val response = baseApiService.getGroupList(group)
            if (response.isSuccessful) {
                val groupList = HtmlParser().parseGroupList(response.body())
                ResponseResult(ResponseType.SUCCESS, data = groupList)
            } else {
                ResponseResult(ResponseType.ERROR, "Ошибка получения списка групп")
            }
        } catch (e: Exception) {
            ResponseResult(ResponseType.ERROR, "Ошибка: ${e.message}")
        }
    }

    suspend fun getCurrentWeekNumber(): Int? {
        return try {
            val response = baseApiService.getCurrentWeekNumber()
            if (response.isSuccessful) {
                HtmlParser().parseCurrentWeekNumber(response.body())
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getHeaderLabel(): String? {
        return try {
            val response = baseApiService.getHeaderLabel()
            if (response.isSuccessful) {
                HtmlParser().parseHeaderLabel(response.body())
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getMessages(tab: String): ResponseResult {
        try {
            val response = ciuApiService.getMessages()
            if (response.isSuccessful) {
                val messages = HtmlParser().parseMessages(response.body(), tab)
                return ResponseResult(ResponseType.SUCCESS, data = messages )
            } else {
                return ResponseResult(ResponseType.ERROR)
            }
        } catch (e: Exception) {
            return ResponseResult(ResponseType.ERROR)
        }
    }

    suspend fun getCoursesSearchResult(query: String): ResponseResult {
        try {
            val response = dispaceApiService.getCoursesSearchResult(query)
            if (response.isSuccessful) {
                return ResponseResult(ResponseType.SUCCESS)
            } else {
                return ResponseResult(ResponseType.ERROR)
            }
        } catch (e: Exception) {
            return ResponseResult(ResponseType.ERROR)
        }
    }

    suspend fun getDispaceSenderList(page: Int = 1): List<Companion>? {
        try {
            val params = HashMap<String?, String?>()
            params["group"] = "dialogs"
            params["page"] = page.toString()
            params["search"] = ""
            params["action"] = "list"

            val response = dispaceApiService.getDispaceSenderList(params)
            if (response.isSuccessful) {
                val companionList = Gson().fromJson(response.body()?.string(), CompanionList::class.java)
                return companionList.list
            } else {
                return null
            }
        } catch (e: Exception) {
            return null
        }
    }

    suspend fun getPersonById(id: String): Person? {
        try {
            val response = ciuApiService.getPersonById(id)
            if (response.isSuccessful) {
                val person = HtmlParser().parsePerson(response.body())
                Log.e("response parser", person.toString())
                return person
            } else {
                Log.e("response parser", "err")
                return null
            }
        } catch (e: Exception) {
            Log.e("response parser", e.stackTraceToString())
            return null
        }
    }

    suspend fun getNews(page: Int): NewsResponse? {
        try {
            val response = baseApiService.getNews(page.toString())
            if (response.isSuccessful) {
                val news = HtmlParser().parseNews(response.body())
                return news
            } else {
                return null
            }
        } catch (e: Exception) {
            Log.e("response parser getNews", e.stackTraceToString())
            return null
        }
    }

    suspend fun getNewsDetail(id: String): NewsDetail? {
        try {
            val response = baseApiService.getNewsDetail(id)
            if (response.isSuccessful) {
                val newsDetail = HtmlParser().parseNewsDetail(response.body())
                return newsDetail
            } else {
                return null
            }
        } catch (e: Exception) {
            Log.e("response parser getNewsDetail", e.stackTraceToString())
            return null
        }
    }

    suspend fun getMessageDetail(id: String): MessageDetail? {
        try {
            val response = ciuApiService.getMessageDetail(id)
            if (response.isSuccessful) {
                val messageDetail = HtmlParser().parseMessageDetail(response.body())
                return messageDetail
            } else {
                return null
            }
        } catch (e: Exception) {
            Log.e("response parser getNewsDetail", e.stackTraceToString())
            return null
        }
    }

    suspend fun getSessiaResults(): List<SessiaResultSemestr>? {
        try {
            val response = ciuApiService.getSessiaResults()
            if (response.isSuccessful) {
                val messageDetail = HtmlParser().parseSessiaResults(response.body())
                return messageDetail
            } else {
                return null
            }
        } catch (e: Exception) {
            Log.e("response parser getNewsDetail", e.stackTraceToString())
            return null
        }
    }

    suspend fun getMoneyYearsList(): List<String>? {
        try {
            val response = ciuApiService.getMoneyYearsList()
            if (response.isSuccessful) {
                val moneyTearList = HtmlParser().parseMoneyYearList(response.body())
                return moneyTearList
            } else {
                return null
            }
        } catch (e: Exception) {
            Log.e("response parser getMoneyYearsList", e.stackTraceToString())
            return null
        }
    }

    suspend fun getMoneyItems(year: String): List<MoneyItem>? {
        try {
            val params = HashMap<String?, String?>()
            params["year"] = year
            params["month"] = "0"
            val response = ciuApiService.getMoneyItems(params)
            if (response.isSuccessful) {
                val moneyItems = HtmlParser().parseMoneyItems(response.body())
                return moneyItems
            } else {
                return null
            }
        } catch (e: Exception) {
            Log.e("response parser getMoneyYearsList", e.stackTraceToString())
            return null
        }
    }

    suspend fun fetchProfileDetail(): ProfileDetail? {
        try {
            val response = ciuApiService.getProfileDetail()
            if (response.isSuccessful) {
                val details = HtmlParser().parseProfileDetail(response.body())
                return details
            } else {
                return null
            }
        } catch (e: Exception) {
            Log.e("", e.stackTraceToString())
            return null
        }
    }

    suspend fun saveProfileDetail(
        n_email: String,
        n_address: String,
        n_phone: String,
        n_snils: String,
        n_oms: String,
        n_vk: String,
        n_tg: String,
        n_leader: String
    ): ProfileDetail? {
        try {

            val params = HashMap<String?, String?>()
            params["save"] = "1"
            params["what"] = "0"
            params["save_oms"] = ""
            params["n_email"] = n_email
            params["n_address"] = n_address
            params["n_phone"] = n_phone
            params["n_snils"] = n_snils
            params["n_oms"] = n_oms
            params["n_vk"] = n_vk
            params["n_tg"] = n_tg
            params["n_leader"] = n_leader
            params["n_has_agree"] = ""


            val response = ciuApiService.saveProfileDetails(params)

            if (response.isSuccessful) {
                val details = HtmlParser().parseProfileDetail(response.body())
                return details
            } else {
                return null
            }
        } catch (e: Exception) {
            Log.e("", e.stackTraceToString())
            return null
        }
    }


}
