package com.dertefter.neticore.network.methods

import android.util.Log
import com.dertefter.neticore.features.schedule.model.Schedule
import com.dertefter.neticore.network.api.BaseApiService
import com.dertefter.neticore.network.parser.HtmlParserSchedule

class BaseMethods(val api: BaseApiService) {

    suspend fun fetchWeekLabel(): String? {
        return try {
            val response = api.fetchWeekLabel()
            if (response.isSuccessful) {
                HtmlParserSchedule().parseWeekLabel(response.body())
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("BaseMethods", e.stackTraceToString())
            null
        }
    }


    suspend fun fetchWeekNumber(): Int? {
        return try {
            val response = api.fetchWeekNumber()
            if (response.isSuccessful) {
                HtmlParserSchedule().parseCurrentWeekNumber(response.body())
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("BaseMethods", e.stackTraceToString())
            null
        }
    }


    suspend fun getWeekNumberListAndFirstDayDate(group: String): Pair<String, List<Int>>? {
        return try {
            val response = api.fetchWeekNumberList(group)
            if (response.isSuccessful) {
                val weekNumberList = HtmlParserSchedule().parseWeekNumberList(response.body())
                weekNumberList
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("BaseMethods", e.stackTraceToString())
            null
        }
    }
    suspend fun fetchScheduleForGuest(
        symGroup: String,
        weekNumberListAndFirstDate: Pair<String, List<Int>>
    ): Schedule? {
        return try {
            val weekNumberList = weekNumberListAndFirstDate.second
            val firstDayDate = weekNumberListAndFirstDate.first

            val response = api.fetchScheduleForGuest(group = symGroup)
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