package dev.medetzhakupov.githubapp.domain.usecase

import dev.medetzhakupov.githubapp.domain.model.Repository
import dev.medetzhakupov.githubapp.domain.repository.GitHubRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRepositoriesUseCase @Inject constructor(
    private val repository: GitHubRepository
) {
    /**
     * Observes repositories from database up to the specified page.
     * This automatically updates when new data arrives.
     */
    fun observeRepositories(maxPage: Int): Flow<List<Repository>> {
        return repository.observeRepositories(maxPage)
    }

    /**
     * Loads a specific page from network and saves to database.
     * Observers will be notified automatically.
     */
    suspend fun loadPage(page: Int, perPage: Int = 30) {
        repository.loadPage(page, perPage)
    }

    /**
     * Refreshes all data (clears cache and loads page 1).
     */
    suspend fun refresh(perPage: Int = 20) {
        repository.refresh(perPage)
    }

    fun getRepositoriesFlow(): Flow<List<Repository>> {
        return repository.getRepositoriesFlow()
    }

    suspend fun getRepositoryById(id: Long): Repository? {
        return repository.getRepositoryById(id)
    }
}