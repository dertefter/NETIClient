package com.dertefter.neticore.network.methods

import android.util.Log
import com.dertefter.neticore.features.person_detail.model.Person
import com.dertefter.neticore.features.schedule.model.Schedule
import com.dertefter.neticore.features.sessia_results.model.SessiaResults
import com.dertefter.neticore.features.user_detail.model.UserDetail
import com.dertefter.neticore.features.user_detail.model.lks
import com.dertefter.neticore.network.api.CiuApiService
import com.dertefter.neticore.network.parser.HtmlParserPerson
import com.dertefter.neticore.network.parser.HtmlParserSchedule
import com.dertefter.neticore.network.parser.HtmlParserSessiaResults
import com.dertefter.neticore.network.parser.HtmlParserUserDetail
import kotlin.toString

class CiuMethods(val api: CiuApiService) {

    suspend fun fetchUserDetail(): UserDetail? {
        return try {
            val response = api.fetchUserDetail()
            if (response.isSuccessful) {
                val userDetail = HtmlParserUserDetail().parseUserDetail(response.body())
                userDetail
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("CiuMethods", e.stackTraceToString())
            null
        }
    }



    suspend fun fetchLksList(): List<lks>? {
        return try {
            val response = api.fetchUserDetail()
            if (response.isSuccessful) {
                return HtmlParserUserDetail().parseLks(response.body())
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("CiuMethods", e.stackTraceToString())
            null
        }
    }

    suspend fun setLksById(id: Int) {
        try {

            val params = HashMap<String?, Int?>()
            params["id_lks"] = id

            val response = api.setLksById(params)
            Log.e("CiuMethods", response.body()?.string().toString())
        } catch (e: Exception) {
            Log.e("CiuMethods", e.stackTraceToString())
            null
        }
    }

    suspend fun fetchSessiaResults(): SessiaResults? {
        return try {
            val response = api.fetchSessiaResults()
            if (response.isSuccessful) {
                val sessiaResults = HtmlParserSessiaResults().parseSessiaResults(response.body())
                sessiaResults
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("CiuMethods", e.stackTraceToString())
            null
        }
    }


    suspend fun fetchPersonById(id: String): Person? {
        try {
            val response = api.fetchPersonById(id)
            if (response.isSuccessful) {
                val person = HtmlParserPerson().parsePerson(response.body())
                return person
            } else {
                Log.e("fetchPersonById", response.code().toString())
                return null
            }
        } catch (e: Exception) {
            Log.e("BaseMethods", e.stackTraceToString())
            return null
        }
    }


    suspend fun fetchSchedule(symGroup: String, weekNumberListAndFirstDate:  Pair<String, List<Int>>): Schedule? {
        return try {

            val weekNumberList = weekNumberListAndFirstDate.second
            val firstDayDate = weekNumberListAndFirstDate.first

            val response = api.fetchSchedule()
            if (response.isSuccessful) {
                val schedule = HtmlParserSchedule().parseSchedule(response.body(), weekNumberList, firstDayDate, symGroup)
                schedule
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("BaseMethods", e.stackTraceToString())
            null
        }
    }


}