package com.example.fotogram.model

import com.example.fotogram.Location
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val userId: Int,
    val sessionId: String
)

@Serializable
data class UpdateUser(
    val username: String,
    val bio: String? = null,
    val dateOfBirth: String? = null
)

@Serializable
data class UpdatePictureUser(
    val base64: String
)

@Serializable
data class SendPost(
    val contentText: String,
    val contentPicture: String?,
    val location: Location = Location()
)
