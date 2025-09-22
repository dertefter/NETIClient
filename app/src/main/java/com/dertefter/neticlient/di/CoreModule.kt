package com.dertefter.neticlient.di

import android.content.Context
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.local.CoreDataStoreManager
import com.dertefter.neticore.local.UserDataStoreManager
import com.dertefter.neticore.network.NetworkClient
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    @Provides
    @Singleton
    fun provideNetworkClient(): NetworkClient = NetworkClient()

    @Provides
    @Singleton
    fun provideUserDataStoreManager(@ApplicationContext context: Context): UserDataStoreManager =
        UserDataStoreManager(context)

    @Provides
    @Singleton
    fun provideCoreDataStoreManager(@ApplicationContext context: Context): CoreDataStoreManager =
        CoreDataStoreManager(context)

    @Provides
    @Singleton
    fun provideNETICore(
        client: NetworkClient,
        userDataStoreManager: UserDataStoreManager,
        coreDataStoreManager: CoreDataStoreManager
    ): NETICore {
        return NETICore(client, userDataStoreManager, coreDataStoreManager)
    }

    @Provides
    @Singleton
    fun providePicasso(
        @ApplicationContext context: Context,
        networkClient: NetworkClient
    ): Picasso {
        return Picasso.Builder(context)
            .downloader(OkHttp3Downloader(networkClient.client))
            .build()
    }
}
