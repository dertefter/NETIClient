package com.dertefter.neticore.network.methods

import android.util.Log
import com.dertefter.neticore.features.inbox.model.Message
import com.dertefter.neticore.features.students.model.StudentGroup
import com.dertefter.neticore.features.user_detail.model.UserDetail
import com.dertefter.neticore.network.api.MobileApiService

class MobileMethods(val api: MobileApiService) {

    suspend fun mobileLogin(params: HashMap<String?, String?>): Boolean? {
        return try {
            val response = api.newAuth(false, params)
            val jsonString = response.body()?.string()!!
            jsonString.contains("login") && jsonString.contains("true")
        } catch (e: Exception) {
            Log.e("MobileMethods", e.stackTraceToString())
            null
        }
    }

    suspend fun fetchUserDetail(): UserDetail? {
        return try {
            val response = api.fetchUserDetail()
            Log.e("MobileMethods", response.toString())
            if (response.isSuccessful) {
                response.body()?.firstOrNull()
            } else {
                Log.e("MobileMethods", "fetchUserDetail failed: ${response.code()} ${response.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e("MobileMethods", e.stackTraceToString())
            null
        }
    }


    suspend fun fetchStudentGroup(id: Int): StudentGroup? {
        return try {
            val response = api.fetchStudentGroup(id.toString())
            Log.e("MobileMethods", response.toString())
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e("MobileMethods", "fetchStudentGroup failed: ${response.code()} ${response.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e("MobileMethods", e.stackTraceToString())
            null
        }
    }


    suspend fun fetchMessages(): List<Message>? {
        return try {
            val response = api.fetchMessages()
            Log.e("MobileMethods", response.toString())
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e("MobileMethods", "fetchMessages failed: ${response.code()} ${response.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e("MobileMethods", e.stackTraceToString())
            null
        }
    }

    suspend fun updateMessageReadStatus(idStudent: Int, idMessage: Int, isRead: Int) {
        try {
            val response = api.updateMessageReadStatus(
                mapOf(
                    "id_student" to idStudent,
                    "id_message" to idMessage,
                    "is_read" to 1
                )
            )

            if (response.isSuccessful) {
                Log.e("MobileMethods", response.toString())
            } else {
                Log.e("MobileMethods", "updateMessageReadStatus failed: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("MobileMethods", e.stackTraceToString())
        }
    }



}