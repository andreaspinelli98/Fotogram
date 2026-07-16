package com.example.fotogram

import kotlinx.serialization.Serializable

//Contiene classi Post e User
@Serializable
data class User(
    val id: Int,
    val createdAt: String,
    val username: String?,
    val bio: String?,
    val dateOfBirth: String?,
    val profilePicture: String?,
    val isYourFollower: Boolean,
    val isYourFollowing: Boolean,
    val followersCount: Int,
    val followingCount: Int,
    val postsCount: Int,
)

@Serializable
data class Post(
    val id: Int,
    val authorId: Int,
    val createdAt: String,
    val contentPicture: String,
    val contentText: String,
    val location: Location? = null
)

@Serializable
data class Location (
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    fun isValid() = latitude != null && longitude != null
}

