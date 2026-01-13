package com.dertefter.neticore.features.students.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class StudentGroup(
    @SerializedName("ID")
    val id: Int,

    @SerializedName("NAME")
    val name: String,

    @SerializedName("FACULTY_NAME_SHORT")
    val facultyNameShort: String,

    @SerializedName("FACULTY_NAME_FULL")
    val facultyNameFull: String,

    @SerializedName("SPECIALIZATION_CODE")
    val specializationCode: String,

    @SerializedName("SPECIALIZATION_NAME")
    val specializationName: String,

    @SerializedName("STUDENTS")
    val students: List<Student>,

) : Parcelable
{

    fun getMonitors(): List<Student> = students.filter { it.isMonitor() }

    fun getMonitorDeputies(): List<Student> = students.filter { it.isMonitorDeputy() }

    fun findStudentById(id: Int): Student? = students.find { it.id == id }

    fun findStudentsBySurname(surname: String): List<Student> =
        students.filter { it.surname.contains(surname, ignoreCase = true) }

    fun getStudentsWithPhotos(): List<Student> = students.filter { it.hasPhoto() }
}