package dev.medetzhakupov.githubapp.domain.model

data class Repository(
    val id: Long,
    val name: String,
    val fullName: String,
    val description: String?,
    val ownerLogin: String,
    val ownerId: Long,
    val ownerAvatarUrl: String,
    val isPrivate: Boolean,
    val visibility: String,
    val htmlUrl: String
)