package dev.medetzhakupov.githubapp.presentation.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.medetzhakupov.githubapp.domain.model.Repository
import dev.medetzhakupov.githubapp.domain.usecase.GetRepositoriesUseCase
import dev.medetzhakupov.githubapp.presentation.ComposeViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

sealed interface RepositoryDetailUiState {
    object Loading : RepositoryDetailUiState
    data class Loaded(
        val repository: Repository,
        val isLoading: Boolean = false,
        val error: String? = null
    ) : RepositoryDetailUiState

    data class Error(val error: String) : RepositoryDetailUiState
}

sealed interface RepositoryDetailEvent {
    data class LoadRepository(val repositoryId: Long) : RepositoryDetailEvent
}

sealed interface RepositoryDetailAction {
    // Add any navigation or other actions here if needed
}

@HiltViewModel
class RepositoryDetailViewModel @Inject constructor(
    private val getRepositoriesUseCase: GetRepositoriesUseCase
) : ComposeViewModel<RepositoryDetailUiState, RepositoryDetailEvent, RepositoryDetailAction>() {

    @Composable
    override fun uiState(
        events: Flow<RepositoryDetailEvent>,
        action: Channel<RepositoryDetailAction>
    ): RepositoryDetailUiState {
        var repository by remember { mutableStateOf<Repository?>(null) }
        var isLoading by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf<String?>(null) }

        // Process events
        LaunchedEffect(Unit) {
            events.collect { event ->
                when (event) {
                    is RepositoryDetailEvent.LoadRepository -> {
                        isLoading = true
                        error = null
                        try {
                            val repo = getRepositoriesUseCase.getRepositoryById(event.repositoryId)
                            if (repo != null) {
                                repository = repo
                            } else {
                                error = "Repository not found"
                            }
                        } catch (e: Exception) {
                            error = e.message ?: "Failed to load repository"
                        } finally {
                            isLoading = false
                        }
                    }
                }
            }
        }

        return when {
            repository != null -> RepositoryDetailUiState.Loaded(
                repository = repository!!,
                isLoading = isLoading,
                error = error
            )

            error != null -> RepositoryDetailUiState.Error(error!!)
            else -> RepositoryDetailUiState.Loading
        }
    }
}