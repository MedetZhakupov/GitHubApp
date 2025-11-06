package dev.medetzhakupov.githubapp.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import dev.medetzhakupov.githubapp.data.database.dao.RepositoryDao
import dev.medetzhakupov.githubapp.data.database.entity.RepositoryEntity

@Database(
    entities = [RepositoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class GitHubDatabase : RoomDatabase() {

    abstract fun repositoryDao(): RepositoryDao

    companion object {
        private const val DATABASE_NAME = "github_database"

        fun create(context: Context): GitHubDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                GitHubDatabase::class.java,
                DATABASE_NAME
            ).build()
        }
    }
}