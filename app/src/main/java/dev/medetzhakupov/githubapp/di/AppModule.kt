package dev.medetzhakupov.githubapp.di

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.medetzhakupov.githubapp.data.database.GitHubDatabase
import dev.medetzhakupov.githubapp.data.database.dao.RepositoryDao
import dev.medetzhakupov.githubapp.data.network.GitHubApi
import dev.medetzhakupov.githubapp.data.repository.GitHubRepositoryImpl
import dev.medetzhakupov.githubapp.domain.repository.GitHubRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl(GitHubApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideGitHubApi(retrofit: Retrofit): GitHubApi {
        return retrofit.create(GitHubApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GitHubDatabase {
        return GitHubDatabase.create(context)
    }

    @Provides
    fun provideRepositoryDao(database: GitHubDatabase): RepositoryDao {
        return database.repositoryDao()
    }

    @Provides
    @Singleton
    fun provideGitHubRepository(
        api: GitHubApi,
        dao: RepositoryDao
    ): GitHubRepository {
        return GitHubRepositoryImpl(api, dao)
    }
}