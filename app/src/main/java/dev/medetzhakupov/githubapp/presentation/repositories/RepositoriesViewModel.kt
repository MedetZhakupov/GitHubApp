package dev.medetzhakupov.githubapp.presentation.repositories

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.medetzhakupov.githubapp.domain.model.Repository
import dev.medetzhakupov.githubapp.domain.usecase.GetRepositoriesUseCase
import dev.medetzhakupov.githubapp.presentation.ComposeViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface RepositoriesUiState {
    object Loading : RepositoriesUiState
    data class Loaded(
        val repositories: List<Repository>,
        val isRefreshing: Boolean,
        val isLoadingMore: Boolean,
        val hasMoreData: Boolean,
        val error: String?
    ) : RepositoriesUiState

    data class Error(val error: String) : RepositoriesUiState
}

sealed interface RepositoriesEvent {
    object Refresh : RepositoriesEvent
    object LoadMore : RepositoriesEvent
    data class OpenRepository(val repositoryId: Long) : RepositoriesEvent
}

sealed interface RepositoriesAction {
    data class NavigateToDetail(val repositoryId: Long) : RepositoriesAction
}

@HiltViewModel
class RepositoriesViewModel @Inject constructor(
    private val getRepositoriesUseCase: GetRepositoriesUseCase
) : ComposeViewModel<RepositoriesUiState, RepositoriesEvent, RepositoriesAction>() {

    private val perPage = 20

    @Composable
    override fun uiState(
        events: Flow<RepositoriesEvent>,
        action: Channel<RepositoriesAction>
    ): RepositoriesUiState {
        var repositories by remember { mutableStateOf<List<Repository>>(emptyList()) }
        var currentPage by remember { mutableIntStateOf(1) }
        var isLoading by remember { mutableStateOf(false) }
        var isLoadingMore by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf<String?>(null) }
        var hasMoreData by remember { mutableStateOf(true) }

        // Observe repositories from database - this will automatically update UI
        LaunchedEffect(currentPage) {
            getRepositoriesUseCase.observeRepositories(currentPage).collect { repos ->
                repositories = repos
            }
        }

        // Load initial data and handle events
        LaunchedEffect(Unit) {
            // Load first page on startup
            if (repositories.isEmpty()) {
                isLoading = true
                try {
                    getRepositoriesUseCase.loadPage(1, perPage)
                    error = null
                } catch (e: Exception) {
                    error = e.message ?: "Failed to load repositories"
                } finally {
                    isLoading = false
                }
            }

            events.collect { event ->
                when (event) {
                    RepositoriesEvent.Refresh -> {
                        isLoading = true
                        error = null
                        try {
                            getRepositoriesUseCase.refresh(perPage)
                            currentPage = 1
                            hasMoreData = true
                        } catch (e: Exception) {
                            error = e.message ?: "Failed to refresh"
                        } finally {
                            isLoading = false
                        }
                    }

                    RepositoriesEvent.LoadMore -> {
                        if (!isLoadingMore && !isLoading && hasMoreData) {
                            isLoadingMore = true
                            val nextPage = currentPage + 1
                            try {
                                getRepositoriesUseCase.loadPage(nextPage, perPage)
                                currentPage = nextPage
                                // If we got fewer items than perPage, no more data
                                hasMoreData = repositories.size >= (nextPage * perPage)
                            } catch (e: Exception) {
                                error = e.message ?: "Failed to load more"
                            } finally {
                                isLoadingMore = false
                            }
                        }
                    }

                    is RepositoriesEvent.OpenRepository -> {
                        // Handle repository navigation
                        action.send(RepositoriesAction.NavigateToDetail(event.repositoryId))
                    }
                }
            }
        }

        return when {
            isLoading && repositories.isEmpty() -> RepositoriesUiState.Loading
            error != null && repositories.isEmpty() -> RepositoriesUiState.Error(error!!)
            else -> RepositoriesUiState.Loaded(
                repositories = repositories,
                isRefreshing = isLoading,
                isLoadingMore = isLoadingMore,
                hasMoreData = hasMoreData,
                error = error
            )
        }
    }
}