package dev.medetzhakupov.githubapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.medetzhakupov.githubapp.presentation.detail.RepositoryDetailScreen
import dev.medetzhakupov.githubapp.presentation.repositories.RepositoriesScreen

@Composable
fun GitHubNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Repositories.route
    ) {
        composable(route = Screen.Repositories.route) {
            RepositoriesScreen(
                onRepositoryClick = { repositoryId ->
                    navController.navigate(Screen.RepositoryDetail.createRoute(repositoryId))
                }
            )
        }

        composable(
            route = Screen.RepositoryDetail.route,
            arguments = Screen.RepositoryDetail.arguments
        ) { backStackEntry ->
            val repositoryId =
                backStackEntry.arguments?.getString(Screen.RepositoryDetail.REPOSITORY_ID_KEY)
                    ?.toLongOrNull()

            if (repositoryId != null) {
                RepositoryDetailScreen(
                    repositoryId = repositoryId,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}