package com.dertefter.neticore


import android.content.Context
import com.dertefter.neticore.features.authorization.AuthorizationFeature
import com.dertefter.neticore.features.control_weeks.ControlWeeksFeature
import com.dertefter.neticore.features.documents.DocumentsFeature
import com.dertefter.neticore.features.documents.NewDocumentFeature
import com.dertefter.neticore.features.inbox.InboxFeature
import com.dertefter.neticore.features.money.MoneyFeature
import com.dertefter.neticore.features.person_detail.PersonDetailFeature
import com.dertefter.neticore.features.schedule.ScheduleFeature
import com.dertefter.neticore.features.sessia_results.SessiaResultsFeature
import com.dertefter.neticore.features.share_sessia_results.ShareSessiaResultsFeature
import com.dertefter.neticore.features.user_detail.UserDetailFeature
import com.dertefter.neticore.local.CoreDataStoreManager
import com.dertefter.neticore.local.UserDataStoreManager
import com.dertefter.neticore.network.NetworkClient
import javax.inject.Inject


class NETICore @Inject constructor(
    client: NetworkClient,
    userDataStoreManager: UserDataStoreManager,
    coreDataStoreManager: CoreDataStoreManager
) {
    val authorizationFeature = AuthorizationFeature(client, userDataStoreManager, coreDataStoreManager)
    val userDetailFeature = UserDetailFeature(client, userDataStoreManager)
    val sessiaResultsFeature = SessiaResultsFeature(client, userDataStoreManager)
    val scheduleFeature = ScheduleFeature(client, userDataStoreManager)
    val shareSessiaResultsFeature = ShareSessiaResultsFeature(client, userDataStoreManager)
    val personDetailFeature = PersonDetailFeature(client, userDataStoreManager)
    val inboxFeature = InboxFeature(client, userDataStoreManager)
    val documentsFeature = DocumentsFeature(client, userDataStoreManager)
    val newDocumentsFeature = NewDocumentFeature(client, userDataStoreManager)
    val controlWeeksFeature = ControlWeeksFeature(client, userDataStoreManager)
    val moneyFeature = MoneyFeature(client, userDataStoreManager)
}