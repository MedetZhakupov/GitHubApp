package dev.medetzhakupov.githubapp.presentation.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(val route: String) {

    object Repositories : Screen("repositories")

    object RepositoryDetail : Screen("repository_detail/{repositoryId}") {
        const val REPOSITORY_ID_KEY = "repositoryId"

        fun createRoute(repositoryId: Long): String {
            return "repository_detail/$repositoryId"
        }

        val arguments: List<NamedNavArgument> = listOf(
            navArgument(REPOSITORY_ID_KEY) {
                type = NavType.StringType
            }
        )
    }
}