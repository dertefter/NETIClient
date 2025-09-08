package com.dertefter.neticlient.di

import android.content.Context
import com.dertefter.neticlient.data.network.ApiService
import com.dertefter.neticlient.data.network.NetworkClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    @Named("default")
    fun provideClient(): OkHttpClient {
        return OkHttpClient.Builder().build()
    }

    @Provides
    @Singleton
    @Named("dispace")
    fun provideDispaceClient(): OkHttpClient {
        return OkHttpClient.Builder().build()
    }

    @Provides
    @Singleton
    @Named("auth")
    fun provideAuthRetrofit(@Named("default") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://login.nstu.ru/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("ciu")
    fun provideCiuRetrofit(@Named("default") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://ciu.nstu.ru/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("base")
    fun provideBaseRetrofit(@Named("default") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://nstu.ru/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("login2auth")
    fun provideTestAuthRetrofit(@Named("default") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://login2.nstu.ru/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }


    @Provides
    @Singleton
    @Named("mobile")
    fun provideMobileRetrofit(@Named("default") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.ciu.nstu.ru/v1.1/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("dispace")
    fun provideDispaceRetrofit(@Named("dispace") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://dispace.edu.nstu.ru/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(@Named("base") retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideNetworkClient(
        @ApplicationContext context: Context,
        @Named("default") client: OkHttpClient,
        @Named("auth") authRetrofit: Retrofit,
        @Named("ciu") ciuRetrofit: Retrofit,
        @Named("base") baseRetrofit: Retrofit,
        @Named("login2auth") login2AuthRetrofit: Retrofit,
        @Named("dispace") dispaceRetrofit: Retrofit,
        @Named("mobile") mobileRetrofit: Retrofit,
    ): NetworkClient {
        return NetworkClient(context, client, authRetrofit, ciuRetrofit, baseRetrofit, login2AuthRetrofit, dispaceRetrofit, mobileRetrofit)
    }
}
