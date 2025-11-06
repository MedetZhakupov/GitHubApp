package dev.medetzhakupov.githubapp.domain.repository

import dev.medetzhakupov.githubapp.domain.model.Repository
import kotlinx.coroutines.flow.Flow

interface GitHubRepository {
    /**
     * Observes repositories from database up to the specified page.
     * This flow automatically updates when new data is inserted.
     */
    fun observeRepositories(maxPage: Int): Flow<List<Repository>>

    /**
     * Loads a specific page from network and saves to database.
     * The UI will be updated automatically via the Flow.
     */
    suspend fun loadPage(page: Int, perPage: Int)

    /**
     * Refreshes all data (clears cache and loads page 1)
     */
    suspend fun refresh(perPage: Int)

    fun getRepositoriesFlow(): Flow<List<Repository>>
    suspend fun getRepositoryById(id: Long): Repository?
}