package com.dertefter.neticore.network

import android.util.Log
import com.dertefter.neticore.network.api.BaseApiService
import com.dertefter.neticore.network.api.CiuApiService
import com.dertefter.neticore.network.api.DispaceApiService
import com.dertefter.neticore.network.api.Login2ApiService
import com.dertefter.neticore.network.api.MobileApiService
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.CookieManager
import java.net.CookiePolicy

class NetworkClient {

    val cookieManager = CookieManager().apply {
        setCookiePolicy(CookiePolicy.ACCEPT_ALL)
    }
    var client: OkHttpClient =  OkHttpClient.Builder().cookieJar(JavaNetCookieJar(cookieManager)).build()


    private val login2Retrofit = Retrofit.Builder()
        .baseUrl("https://login2.nstu.ru/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val baseRetrofit = Retrofit.Builder()
        .baseUrl("https://nstu.ru/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val ciuRetrofit = Retrofit.Builder()
        .baseUrl("https://ciu.nstu.ru/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val dispaceRetrofit = Retrofit.Builder()
        .baseUrl("https://dispace.edu.nstu.ru/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val mobileRetrofit = Retrofit.Builder()
        .baseUrl("https://api.ciu.nstu.ru/v1.1/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()


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



    fun rebuildClient() {
        Log.e("NetworkClient", "rebuilding client...")
        val newCookieManager = CookieManager().apply {
            setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        }
        client = OkHttpClient.Builder().cookieJar(JavaNetCookieJar(newCookieManager)).build()
    }



}