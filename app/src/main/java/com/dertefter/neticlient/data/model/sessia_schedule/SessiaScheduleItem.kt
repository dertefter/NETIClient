package com.dertefter.neticlient.data.model.sessia_schedule

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

data class SessiaScheduleItem(
    val name: String,
    val time: String?,
    val dateString: String,
    val type: String,
    val aud: String,
    val personIds: List<String>
) {
    fun getDate(): LocalDate {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yy")
        return LocalDate.parse(dateString, formatter)
    }

    fun getTime(): LocalTime? {
        return try {
            time?.let {
                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                LocalTime.parse(it, timeFormatter)
            }
        } catch (e: DateTimeParseException) {
            null
        }
    }
}