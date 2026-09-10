package com.dertefter.data.dto.user

import kotlinx.serialization.Serializable

@Serializable
data class ContactInfoDto(
    val name: String? = null,
    val surname: String? = null,
    val patronymic: String? = null,
    val symGroup: String? = null,
    val email: String? = null,
    val address: String? = null,
    val mobilePhoneNumber: String? = null,
    val snils: String? = null,
    val oms: String? = null,
    val vk: String? = null,
    val telegram: String? = null,
    val leaderId: String? = null
)


fun ContactInfoDto.toUserInfoDto(): UserInfoDto {
    return UserInfoDto(
        address = address,
        email = email,
        leaderId = leaderId,
        mobilePhoneNumber = mobilePhoneNumber,
        name = name,
        surname = surname,
        patronymic = patronymic,
        snils = snils,
        symGroup = symGroup,
        tg = telegram,
        vk = vk
    )
}