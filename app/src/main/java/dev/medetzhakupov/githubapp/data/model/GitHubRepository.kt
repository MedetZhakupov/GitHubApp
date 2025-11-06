package dev.medetzhakupov.githubapp.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GitHubRepository(
    @Json(name = "id")
    val id: Long,

    @Json(name = "name")
    val name: String,

    @Json(name = "full_name")
    val fullName: String,

    @Json(name = "description")
    val description: String?,

    @Json(name = "owner")
    val owner: Owner,

    @Json(name = "private")
    val private: Boolean,

    @Json(name = "visibility")
    val visibility: String,

    @Json(name = "html_url")
    val htmlUrl: String
)

@JsonClass(generateAdapter = true)
data class Owner(
    @Json(name = "login")
    val login: String,

    @Json(name = "id")
    val id: Long,

    @Json(name = "avatar_url")
    val avatarUrl: String
)