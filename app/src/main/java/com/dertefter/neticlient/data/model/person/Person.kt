package com.dertefter.neticlient.data.model.person

data class Person(
    val name: String,
    val avatarUrl: String?,
    val about_disc: String?,
    val phone: String?,
    val email: String?,
    val address: String?,
    val profiles: String?,
    val disceplines: String?,
    val hasTimetable: Boolean,
) {
    fun getShortName(): String {
        try{
            val full = name.split(" ")
            val surname = full[0]
            val first_name = full[1]
            val last_name = full[2]
            return "$surname ${first_name.get(0)}. ${last_name.get(0)}."
        }catch (e: Exception){
            return ""
        }
    }
}
