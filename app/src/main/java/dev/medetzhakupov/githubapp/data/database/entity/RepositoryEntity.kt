package dev.medetzhakupov.githubapp.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "repositories")
data class RepositoryEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val fullName: String,
    val description: String?,
    val ownerLogin: String,
    val ownerId: Long,
    val ownerAvatarUrl: String,
    val isPrivate: Boolean,
    val visibility: String,
    val htmlUrl: String,
    val page: Int = 1, // Track which page this repository belongs to
    val indexInPage: Int = 0 // Track order within the page
)