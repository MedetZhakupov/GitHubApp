package dev.medetzhakupov.githubapp.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.medetzhakupov.githubapp.data.database.entity.RepositoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RepositoryDao {

    @Query("SELECT * FROM repositories ORDER BY page ASC, indexInPage ASC LIMIT :limit OFFSET :offset")
    suspend fun getRepositoriesPaged(limit: Int, offset: Int): List<RepositoryEntity>

    @Query("SELECT * FROM repositories ORDER BY page ASC, indexInPage ASC")
    fun getAllRepositories(): Flow<List<RepositoryEntity>>

    @Query("SELECT * FROM repositories WHERE page <= :maxPage ORDER BY page ASC, indexInPage ASC")
    fun getRepositoriesUpToPage(maxPage: Int): Flow<List<RepositoryEntity>>

    @Query("SELECT * FROM repositories WHERE id = :id")
    suspend fun getRepositoryById(id: Long): RepositoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepositories(repositories: List<RepositoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepository(repository: RepositoryEntity)

    @Query("DELETE FROM repositories")
    suspend fun clearAll()

    @Query("DELETE FROM repositories WHERE page = :page")
    suspend fun clearPage(page: Int)

    @Query("SELECT COUNT(*) FROM repositories")
    suspend fun getRepositoryCount(): Int

    @Query("SELECT MAX(page) FROM repositories")
    suspend fun getMaxPage(): Int?
}