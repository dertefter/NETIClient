package com.dertefter.neticlient.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dertefter.neticore.features.person_detail.model.Person
import com.dertefter.neticlient.data.network.NetworkClient
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.network.model.ResponseType
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PersonRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val networkClient: NetworkClient
) {

    suspend fun fetchPersonById(id: String, forceOffline: Boolean = true): ResponseResult {
        if (getSavedPerson(id).first() != null && forceOffline){
            val person = getSavedPerson(id).first()
            return ResponseResult(ResponseType.SUCCESS, data = person)
        }
        val person = networkClient.getPersonById(id)
        if (person != null) {
            if (person.name.isNotEmpty()){
                savePerson(id, person)
            }
            return ResponseResult(ResponseType.SUCCESS, data = person)
        }
        return ResponseResult(ResponseType.ERROR)
    }

    private suspend fun savePerson(id: String, person: Person) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("person_id$id")] = Gson().toJson(person)
        }
    }

    fun getSavedPerson(id: String): Flow<Person?> {
        return dataStore.data.map { preferences ->
            val personJson = preferences[stringPreferencesKey("person_id$id")]
            if (personJson != null) {
                Gson().fromJson(personJson, Person::class.java)
            } else {
                null
            }
        }
    }

    suspend fun fetchPersonSearchResults(q: String): ResponseResult {
        val personIdList = networkClient.getPersonSearchResults(q)
        if (personIdList as List<Pair<String, String>>? != null) {
            return ResponseResult(ResponseType.SUCCESS, data = personIdList as List<Pair<String, String>>)
        }
        return ResponseResult(ResponseType.ERROR)
    }



}