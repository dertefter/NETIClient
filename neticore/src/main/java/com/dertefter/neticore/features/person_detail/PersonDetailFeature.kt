package com.dertefter.neticore.features.person_detail

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dertefter.neticore.features.person_detail.model.Person
import com.dertefter.neticore.local.UserDataStoreManager
import com.dertefter.neticore.network.NetworkClient
import com.dertefter.neticore.network.methods.CiuMethods
import com.google.gson.Gson
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class PersonDetailFeature(
    val client: NetworkClient,
    val userDataStoreManager: UserDataStoreManager
) {

    private val ciuMethods = CiuMethods(client.ciuApiService)
    private val gson = Gson()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun personById(personId: String): Flow<Person?> {
        return userDataStoreManager.currentStore
            .flatMapLatest { dataStore ->
                dataStore.data.map { prefs ->
                    prefs[stringPreferencesKey("person_$personId")]?.let { json ->
                        try {
                            gson.fromJson(json, Person::class.java)
                        } catch (e: Exception) {
                            null
                        }
                    }
                }
            }
    }



    private suspend fun savePerson(personId: String, person: Person) {
        val dataStore = userDataStoreManager.currentStore.first()
        dataStore.edit { preferences ->
            val key = stringPreferencesKey("person_$personId")
            val personJson = gson.toJson(person)
            preferences[key] = personJson
        }
    }

    suspend fun updatePersonById(personId: String){
        val person = ciuMethods.fetchPersonById(personId)
        if (person != null){
            savePerson(personId, person)
        }

    }



}
