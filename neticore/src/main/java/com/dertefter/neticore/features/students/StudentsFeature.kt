package com.dertefter.neticore.features.students

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dertefter.neticore.features.students.model.StudentGroup
import com.dertefter.neticore.local.UserDataStoreManager
import com.dertefter.neticore.network.NetworkClient
import com.dertefter.neticore.network.ResponseType
import com.dertefter.neticore.network.methods.MobileMethods
import com.google.gson.Gson
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class StudentsFeature(
    val client: NetworkClient,
    val userDataStoreManager: UserDataStoreManager
) {

    private val mobileMethods = MobileMethods(client.mobileApiService)
    private val gson = Gson()

    val STUDENT_GROUP_KEY = stringPreferencesKey("student_group")

    val status = MutableStateFlow(ResponseType.LOADING)

    @OptIn(ExperimentalCoroutinesApi::class)
    val studentGroup: Flow<StudentGroup?> = userDataStoreManager.currentStore
        .flatMapLatest { dataStore ->
            dataStore.data.map { prefs ->
                prefs[STUDENT_GROUP_KEY]?.let { json ->
                    try {
                        gson.fromJson(json, StudentGroup::class.java)
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        }



    suspend fun updateStudentGroup(id: Int?){
        if (id == null){
            status.value = ResponseType.SUCCESS
            return
        }

        status.value = ResponseType.LOADING

        val studentGroup = mobileMethods.fetchStudentGroup(id)

        if (studentGroup != null){
            userDataStoreManager.currentStore.value.edit { prefs ->
                prefs[STUDENT_GROUP_KEY] = gson.toJson(studentGroup)
            }
            status.value = ResponseType.SUCCESS
        } else {
            status.value = ResponseType.ERROR
        }



    }



}
