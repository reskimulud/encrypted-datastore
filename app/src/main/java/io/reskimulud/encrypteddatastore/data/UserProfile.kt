package io.reskimulud.encrypteddatastore.data

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val apiKey: String = "",
)
