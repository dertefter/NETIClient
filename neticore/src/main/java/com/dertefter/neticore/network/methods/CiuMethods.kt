package com.dertefter.neticore.network.methods

import android.util.Log
import com.dertefter.neticore.features.control_weeks.model.ControlResult
import com.dertefter.neticore.features.documents.model.DocumentOptionItem
import com.dertefter.neticore.features.documents.model.DocumentRequestItem
import com.dertefter.neticore.features.documents.model.DocumentsItem
import com.dertefter.neticore.features.money.model.MoneyItem
import com.dertefter.neticore.features.person_detail.model.Person
import com.dertefter.neticore.features.schedule.model.Schedule
import com.dertefter.neticore.features.sessia_results.model.SessiaResults
import com.dertefter.neticore.features.user_detail.model.UserDetail
import com.dertefter.neticore.features.user_detail.model.lks
import com.dertefter.neticore.network.api.CiuApiService
import com.dertefter.neticore.network.parser.HtmlParserDocuments
import com.dertefter.neticore.network.parser.HtmlParserMoney
import com.dertefter.neticore.network.parser.HtmlParserPerson
import com.dertefter.neticore.network.parser.HtmlParserSchedule
import com.dertefter.neticore.network.parser.HtmlParserSessiaResults
import com.dertefter.neticore.network.parser.HtmlParserUserDetail
import org.jsoup.Jsoup
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

    suspend fun fetchControlWeeks():  ControlResult? {
        try {
            val response = api.fetchControlWeeks()
            if (response.isSuccessful) {
                val s = HtmlParserSessiaResults().parseControlWeeks(response.body())
                return s
            } else {
                return null
            }
        } catch (e: Exception) {
            return null
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


    suspend fun fetchDocumentList(): List<DocumentsItem>? {
        try {
            val response = api.fetchDocuments()

            if (response.isSuccessful) {
                val documentList = HtmlParserDocuments().parseDocumentList(response.body())
                return documentList
            } else {
                return null
            }
        } catch (e: Exception) {
            return null
        }
    }

    suspend fun fetchDocumentOptionList(): List<DocumentOptionItem>? {
        try {
            val response = api.fetchDocuments()

            if (response.isSuccessful) {
                val documentOptionList = HtmlParserDocuments().parseDocumentOptionsList(response.body())
                return documentOptionList
            } else {
                return null
            }
        } catch (e: Exception) {
            return null
        }
    }


    suspend fun getDocumentRequestItem(value: String): DocumentRequestItem? {
        try {

            val params = HashMap<String?, String?>()
            params["ajax"] = "1"
            params["type_doc"] = value

            val response = api.getDocumentRequestItem(params)

            if (response.isSuccessful) {
                val d = HtmlParserDocuments().parseDocumentRequest(response.body())
                return d
            } else {
                return null
            }
        } catch (e:Exception) {
            return null
        }
    }

    suspend fun claimNewDocument(typeClaim: String, comment: String): Boolean? {
        try {
            val params = HashMap<String?, String?>()
            params["what"] = "1"
            params["send"] = "1"
            params["type_claim"] = typeClaim
            params["file_pay"] = ""
            params["file_zayav"] = ""
            params["comment"] = comment
            val response = api.claimNewDocument(params)

            if (response.isSuccessful) {
                return true
            } else {
                return null
            }
        } catch (e: Exception) {
            return null
        }
    }

    suspend fun checkCancelable(id: String): Boolean? {
        try {
            val response = api.checkDocCancelable(id)
            if (response.isSuccessful) {
                val pretty = response.body()?.string().toString()
                var doc = Jsoup.parse(pretty)
                val body = doc.body().toString()
                if (body.contains("Удалить заявку")){
                    return true
                } else {
                    return false
                }
            } else {
                return null
            }
        } catch (e: Exception) {
            return null
        }
    }

    suspend fun cancelDocument(id: String?): Boolean? {
        try {
            val params = HashMap<String?, String?>()
            params["act"] = "2"
            val response = api.cancelDocument(id, params)
            if (response.isSuccessful) {
                return true
            } else {
                return null
            }
        } catch (e: Exception) {
            return null
        }
    }


    suspend fun fetchShareScore():  String? {
        try {
            val response = api.getShareScore()
            if (response.isSuccessful) {
                val s = HtmlParserSessiaResults().parseShareScore(response.body())
                return s
            } else {
                return null
            }
        } catch (e: Exception) {
            return null
        }
    }

    suspend fun fetchRequestNewLink() {
        try {
            val params: HashMap<String?, String?> = HashMap<String?, String?>()
            params["generate_access_url"] = "true"
            api.fetchRequestNewLink(params)
        } catch (e: Exception) {

        }
    }


    suspend fun fetchMoneyYearsList(): List<String>? {
        try {
            val response = api.getMoneyYearsList()
            if (response.isSuccessful) {
                val moneyTearList = HtmlParserMoney().parseMoneyYearList(response.body())
                return moneyTearList
            } else {
                return null
            }
        } catch (e: Exception) {
            return null
        }
    }

    suspend fun fetchMoneyItems(year: String): List<MoneyItem>? {
        try {
            val params = HashMap<String?, String?>()
            params["year"] = year
            params["month"] = "0"
            val response = api.getMoneyItems(params)
            if (response.isSuccessful) {
                val moneyItems = HtmlParserMoney().parseMoneyItems(response.body())
                return moneyItems
            } else {
                return null
            }
        } catch (e: Exception) {
            return null
        }
    }




}