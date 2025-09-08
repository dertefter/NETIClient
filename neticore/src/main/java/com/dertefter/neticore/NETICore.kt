package com.dertefter.neticore


import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.dertefter.neticore.features.authorization.AuthorizationFeature
import com.dertefter.neticore.features.inbox.InboxFeature
import com.dertefter.neticore.features.person_detail.PersonDetailFeature
import com.dertefter.neticore.features.schedule.ScheduleFeature
import com.dertefter.neticore.features.sessia_results.SessiaResultsFeature
import com.dertefter.neticore.features.user_detail.UserDetailFeature
import com.dertefter.neticore.local.CoreDataStoreManager
import com.dertefter.neticore.local.UserDataStoreManager
import com.dertefter.neticore.network.NetworkClient


@SuppressLint("StaticFieldLeak")
object NETICore {

    private lateinit var applicationContext: Context
    private lateinit var userDataStoreManager: UserDataStoreManager
    private lateinit var coreDataStoreManager: CoreDataStoreManager


    val client = NetworkClient()

    lateinit var authorizationFeature: AuthorizationFeature
    lateinit var userDetailFeature: UserDetailFeature
    lateinit var scheduleFeature: ScheduleFeature
    lateinit var sessiaResultsFeature: SessiaResultsFeature
    lateinit var personDetailFeature: PersonDetailFeature

    lateinit var inboxFeature: InboxFeature


    fun setupWith(application: Application) {
        applicationContext = application.applicationContext
        userDataStoreManager = UserDataStoreManager(applicationContext)
        coreDataStoreManager = CoreDataStoreManager(applicationContext)
        authorizationFeature = AuthorizationFeature(client, userDataStoreManager, coreDataStoreManager)
        userDetailFeature = UserDetailFeature(client, userDataStoreManager)
        sessiaResultsFeature = SessiaResultsFeature(client, userDataStoreManager)
        scheduleFeature = ScheduleFeature(client, userDataStoreManager)
        personDetailFeature = PersonDetailFeature(client, userDataStoreManager)
        inboxFeature = InboxFeature(client, userDataStoreManager)

    }

}