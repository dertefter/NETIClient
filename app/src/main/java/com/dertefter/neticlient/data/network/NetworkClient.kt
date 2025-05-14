package com.dertefter.neticlient.data.network

import android.content.Context
import android.util.Log
import com.dertefter.neticlient.data.model.UserInfo
import com.dertefter.neticlient.data.model.dispace.messages.Companion
import com.dertefter.neticlient.data.model.dispace.messages.CompanionList
import com.dertefter.neticlient.data.model.documents.DocumentOptionItem
import com.dertefter.neticlient.data.model.documents.DocumentRequestItem
import com.dertefter.neticlient.data.model.documents.DocumentsItem
import com.dertefter.neticlient.data.model.messages.MessageDetail
import com.dertefter.neticlient.data.model.money.MoneyItem
import com.dertefter.neticlient.data.model.news.NewsDetail
import com.dertefter.neticlient.data.model.news.NewsResponse
import com.dertefter.neticlient.data.model.news.PromoItem
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
import org.jsoup.Jsoup
import retrofit2.Retrofit
import java.io.File
import java.io.FileOutputStream
import java.net.CookieManager
import java.net.CookiePolicy
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
    @Named("testAuth") private var testAuthRetrofit: Retrofit,
    @Named("dispace") private var dispaceRetrofit: Retrofit
) {
    var authApiService: ApiService = authRetrofit.create(ApiService::class.java)
    var ciuApiService: ApiService = ciuRetrofit.create(ApiService::class.java)
    var baseApiService: ApiService = baseRetrofit.create(ApiService::class.java)
    var testAuthApiService: ApiService = testAuthRetrofit.create(ApiService::class.java)
    var dispaceApiService: ApiService = dispaceRetrofit.create(ApiService::class.java)

    fun rebuildClientWithToken(token: String?) {

        val cookieManager = CookieManager()
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)

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
        client = OkHttpClient.Builder().cookieJar(JavaNetCookieJar(cookieManager)).build()

        dispaceClient = OkHttpClient.Builder().cookieJar(cookieJar)
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
        testAuthRetrofit = testAuthRetrofit.newBuilder().client(client).build()
        dispaceRetrofit = dispaceRetrofit.newBuilder().client(dispaceClient).build()

        authApiService = authRetrofit.create(ApiService::class.java)
        ciuApiService = ciuRetrofit.create(ApiService::class.java)
        baseApiService = baseRetrofit.create(ApiService::class.java)
        dispaceApiService = dispaceRetrofit.create(ApiService::class.java)
        testAuthApiService = testAuthRetrofit.create(ApiService::class.java)
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
       try {
           rebuildClientWithToken(null)
           val response1 = testAuthApiService.tst1()
           val respBody1 = response1.body()?.string().toString()
           if (respBody1 != null){
                val params = HtmlParser().extractFormParams(respBody1)
                val paramLoginPassword = HashMap<String?, String?>()
               paramLoginPassword["username"] = login
               paramLoginPassword["selected_subset"] = ""
               paramLoginPassword["username-visible"] = login
               paramLoginPassword["password"] = password
               paramLoginPassword["credentialId"] = ""

                val resp2 = testAuthApiService.tst2(
                    session_code = params.get("session_code")!!,
                    execution = params.get("execution")!!,
                    client_id = params.get("client_id")!!,
                    tab_id = params.get("tab_id")!!,
                    client_data = params.get("client_data")!!,
                    params = paramLoginPassword
                )
                val uI = getUserInfo()
               if (uI.data as UserInfo? != null) return ResponseResult(ResponseType.SUCCESS, "Успешный вход")

           }

           return ResponseResult(ResponseType.ERROR, "Ошибка сети:")
        } catch (e: Exception) {
            Log.e("eeee", e.stackTraceToString())
            return ResponseResult(ResponseType.ERROR, "Ошибка сети: ${e.message}")
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

    suspend fun getWeekNumberListAndFirstDayDate(group: String): Pair<String, List<Int>>? {
        return try {
            val response = baseApiService.getWeekNumberList(group)
            if (response.isSuccessful) {
                val weekNumberList = HtmlParser().parseWeekNumberList(response.body())
                weekNumberList
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getSchedule(group: String, isIndividual: Boolean = false): ResponseResult {
        return try {
            val weekNumberListAndFirstDate = getWeekNumberListAndFirstDayDate(group)!!
            val weekNumberList = weekNumberListAndFirstDate.second
            val firstDayDate = weekNumberListAndFirstDate.first

            val response = if (isIndividual){
                ciuApiService.getIndividualSchedule()
            }else{
                baseApiService.getSchedule(group)
            }

            if (response.isSuccessful) {
                val schedule = HtmlParser().parseSchedule(response.body(), weekNumberList, firstDayDate)
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

    suspend fun getMessagesCount(): ResponseResult {
        try {
            val response = ciuApiService.getMessages()
            if (response.isSuccessful) {
                val mes_count = HtmlParser().parseMessagesCount(response.body())
                return ResponseResult(ResponseType.SUCCESS, data = mes_count )
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
                return person
            } else {
                
                return null
            }
        } catch (e: Exception) {
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
            return null
        }
    }

    suspend fun getPromoList(): List<PromoItem>? {
        try {
            val response = baseApiService.getPromoList()
            if (response.isSuccessful) {
                val promoList = HtmlParser().parsePromoList(response.body())
                return promoList
            } else {
                return null
            }
        } catch (e: Exception) {
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
            return null
        }
    }

    suspend fun getPersonSearchResults(q: String): List<Pair<String, String>>? {
        try {
            val response = baseApiService.findPerson(q)

            if (response.isSuccessful) {
                val personIdList = HtmlParser().parsePersonSearchResults(response.body())
                return personIdList
            } else {
                return null
            }
        } catch (e: Exception) {
            return null
        }
    }

    suspend fun getDocumentList(): List<DocumentsItem>? {
        try {
            val response = ciuApiService.getDocuments()

            if (response.isSuccessful) {
                val documentList = HtmlParser().parseDocumentList(response.body())
                return documentList
            } else {
                return null
            }
        } catch (e: Exception) {
            return null
        }
    }

    suspend fun getDocumentOptionList(): List<DocumentOptionItem>? {
        try {
            val response = ciuApiService.getDocuments()

            if (response.isSuccessful) {
                val documentOptionList = HtmlParser().parseDocumentOptionsList(response.body())
                return documentOptionList
            } else {
                return null
            }
        } catch (e: Exception) {
            return null
        }
    }

    suspend fun getDocumentRequestItem(value: String): DocumentRequestItem? {
        try {

            val params = HashMap<String?, String?>()
            params["ajax"] = "1"
            params["type_doc"] = value

            val response = ciuApiService.getDocumentRequestItem(params)

            if (response.isSuccessful) {
                val d = HtmlParser().parseDocumentRequest(response.body())
                return d
            } else {
                return null
            }
        } catch (e:Exception) {
            return null
        }
    }

    suspend fun claimNewDocument(typeClaim: String, comment: String): Boolean? {
        try {
            val params = HashMap<String?, String?>()
            params["what"] = "1"
            params["send"] = "1"
            params["type_claim"] = typeClaim
            params["file_pay"] = ""
            params["file_zayav"] = ""
            params["comment"] = comment
            val response = ciuApiService.claimNewDocument(params)

            if (response.isSuccessful) {
                return true
            } else {
                return null
            }
        } catch (e: Exception) {
            return null
        }
    }

    suspend fun checkCancelable(id: String): Boolean? {
        try {
            val response = ciuApiService.checkDocCancelable(id)
            if (response.isSuccessful) {
                val pretty = response.body()?.string().toString()
                var doc = Jsoup.parse(pretty)
                val body = doc.body().toString()
                if (body.contains("Удалить заявку")){
                    return true
                } else {
                    return false
                }
            } else {
                return null
            }
        } catch (e: Exception) {
            return null
        }
    }

    suspend fun cancelDocument(id: String?): Boolean? {
        try {
            val params = HashMap<String?, String?>()
            params["act"] = "2"
            val response = ciuApiService.cancelDocument(id, params)
            if (response.isSuccessful) {
                return true
            } else {
                return null
            }
        } catch (e: Exception) {
            return null
        }
    }


}
