package dev.medetzhakupov.githubapp.data.network

import dev.medetzhakupov.githubapp.data.model.GitHubRepository
import retrofit2.http.GET
import retrofit2.http.Query

interface GitHubApi {

    @GET("users/abnamrocoesd/repos")
    suspend fun getRepositories(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10
    ): List<GitHubRepository>

    companion object {
        const val BASE_URL = "https://api.github.com/"
    }
}