package com.dertefter.neticore.network

import android.util.Log
import com.dertefter.neticore.network.api.BaseApiService
import com.dertefter.neticore.network.api.CiuApiService
import com.dertefter.neticore.network.api.DispaceApiService
import com.dertefter.neticore.network.api.Login2ApiService
import com.dertefter.neticore.network.api.MobileApiService
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.CookieManager
import java.net.CookiePolicy

class NetworkClient {

    private val cookieManager = CookieManager().apply {
        setCookiePolicy(CookiePolicy.ACCEPT_ALL)
    }

    val client: OkHttpClient = OkHttpClient.Builder()
        .cookieJar(JavaNetCookieJar(cookieManager))
        .build()

    private val login2Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://login2.nstu.ru/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val baseRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://nstu.ru/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val ciuRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://ciu.nstu.ru/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val dispaceRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://dispace.edu.nstu.ru/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val mobileRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.ciu.nstu.ru/v1.1/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }


    val login2ApiService: Login2ApiService
        get() = login2Retrofit.create(Login2ApiService::class.java)

    val ciuApiService: CiuApiService
        get() = ciuRetrofit.create(CiuApiService::class.java)

    val baseApiService: BaseApiService
        get() = baseRetrofit.create(BaseApiService::class.java)

    val dispaceApiService: DispaceApiService
        get() = dispaceRetrofit.create(DispaceApiService::class.java)

    val mobileApiService: MobileApiService
        get() = mobileRetrofit.create(MobileApiService::class.java)

    fun clearSession() {
        Log.d("NetworkClient", "Clearing session cookies...")
        cookieManager.cookieStore.removeAll()
        Log.d("NetworkClient", "Session cookies cleared!")
    }
}