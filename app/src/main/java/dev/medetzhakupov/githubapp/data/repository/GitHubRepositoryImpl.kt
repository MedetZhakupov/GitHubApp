package dev.medetzhakupov.githubapp.data.repository

import dev.medetzhakupov.githubapp.data.database.dao.RepositoryDao
import dev.medetzhakupov.githubapp.data.database.entity.RepositoryEntity
import dev.medetzhakupov.githubapp.data.model.GitHubRepository
import dev.medetzhakupov.githubapp.data.network.GitHubApi
import dev.medetzhakupov.githubapp.domain.model.Repository
import dev.medetzhakupov.githubapp.domain.repository.GitHubRepository as DomainRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GitHubRepositoryImpl @Inject constructor(
    private val api: GitHubApi,
    private val dao: RepositoryDao
) : DomainRepository {

    /**
     * Returns a reactive Flow that observes repositories up to the specified page.
     * UI automatically updates when database changes.
     */
    override fun observeRepositories(maxPage: Int): Flow<List<Repository>> {
        return dao.getRepositoriesUpToPage(maxPage).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Loads a specific page from the network and inserts into database.
     * The Flow returned by observeRepositories() will automatically emit the new data.
     */
    override suspend fun loadPage(page: Int, perPage: Int) {
        try {
            val networkRepos = api.getRepositories(page, perPage)
            val entities = networkRepos.mapIndexed { index, repo ->
                repo.toEntity(page = page, indexInPage = index)
            }

            // Insert new page data into database
            dao.insertRepositories(entities)
        } catch (e: Exception) {
            // Let the error propagate to the caller
            // Cached data will still be available via the Flow
            throw e
        }
    }

    /**
     * Refreshes data by clearing the cache and loading page 1.
     */
    override suspend fun refresh(perPage: Int) {
        try {
            // Clear all existing data
            dao.clearAll()

            // Load first page
            val networkRepos = api.getRepositories(page = 1, perPage = perPage)
            val entities = networkRepos.mapIndexed { index, repo ->
                repo.toEntity(page = 1, indexInPage = index)
            }

            dao.insertRepositories(entities)
        } catch (e: Exception) {
            // If refresh fails and DB is empty, propagate the error
            throw e
        }
    }

    override fun getRepositoriesFlow(): Flow<List<Repository>> {
        return dao.getAllRepositories().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getRepositoryById(id: Long): Repository? {
        return dao.getRepositoryById(id)?.toDomainModel()
    }
}

// Extension functions for mapping between data models
private fun GitHubRepository.toEntity(page: Int, indexInPage: Int): RepositoryEntity {
    return RepositoryEntity(
        id = id,
        name = name,
        fullName = fullName,
        description = description,
        ownerLogin = owner.login,
        ownerId = owner.id,
        ownerAvatarUrl = owner.avatarUrl,
        isPrivate = private,
        visibility = visibility,
        htmlUrl = htmlUrl,
        page = page,
        indexInPage = indexInPage
    )
}

private fun RepositoryEntity.toDomainModel(): Repository {
    return Repository(
        id = id,
        name = name,
        fullName = fullName,
        description = description,
        ownerLogin = ownerLogin,
        ownerId = ownerId,
        ownerAvatarUrl = ownerAvatarUrl,
        isPrivate = isPrivate,
        visibility = visibility,
        htmlUrl = htmlUrl
    )
}