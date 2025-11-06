package dev.medetzhakupov.githubapp.presentation.repositories

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.medetzhakupov.githubapp.domain.model.Repository

@Composable
fun RepositoriesScreen(
    onRepositoryClick: (Long) -> Unit,
    viewModel: RepositoriesViewModel = hiltViewModel()
) {
    val uiState by viewModel.ui.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.action.collect {
            when(it) {
                is RepositoriesAction.NavigateToDetail -> onRepositoryClick(it.repositoryId)
            }
        }
    }

    RepositoriesContent(uiState) {
        viewModel.onEvent(it)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepositoriesContent(
    uiState: RepositoriesUiState,
    onEvent: (RepositoriesEvent) -> Unit
) {
    val listState = rememberLazyListState()

    // Handle pagination - load more when scrolled to near the end
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (uiState is RepositoriesUiState.Loaded) {
                    if (lastVisibleIndex != null &&
                        lastVisibleIndex >= uiState.repositories.size - 3 &&
                        uiState.hasMoreData &&
                        !uiState.isRefreshing &&
                        !uiState.isLoadingMore
                    ) {
                        onEvent(RepositoriesEvent.LoadMore)
                    }
                }
            }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "ABN AMRO Repositories",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            actions = {
                IconButton(
                    onClick = { onEvent(RepositoriesEvent.Refresh) },
                    enabled = uiState !is RepositoriesUiState.Loaded || !uiState.isRefreshing
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }
        )

        // Content
        Box(modifier = Modifier.fillMaxSize()) {
            when (uiState) {
                RepositoriesUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = "Loading repositories...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                is RepositoriesUiState.Loaded -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.repositories,
                            key = { it.id }
                        ) { repository ->
                            RepositoryItem(
                                repository = repository,
                                onClick = { onEvent(RepositoriesEvent.OpenRepository(repository.id)) }
                            )
                        }

                        // Loading more indicator
                        if (uiState.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        // End of list indicator (no more data)
                        if (!uiState.hasMoreData && uiState.repositories.isNotEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No more repositories",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Show error snackbar if there's an error while data is visible
                    if (uiState.error != null) {
                        LaunchedEffect(uiState.error) {
                            // You can show a Snackbar here if you have a SnackbarHost
                            // For now, we'll just display it at the bottom
                        }
                    }
                }
                is RepositoriesUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Failed to load repositories",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                text = uiState.error,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(
                                onClick = { onEvent(RepositoriesEvent.Refresh) }
                            ) {
                                Text("Try Again")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RepositoryItem(
    repository: Repository,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Owner avatar
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(repository.ownerAvatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Owner avatar",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Repository info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = repository.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = repository.ownerLogin,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (repository.description != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = repository.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Visibility and privacy indicators
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Privacy indicator
                    Surface(
                        color = if (repository.isPrivate) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (repository.isPrivate) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            Text(
                                text = if (repository.isPrivate) "Private" else "Public",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (repository.isPrivate) {
                                    MaterialTheme.colorScheme.onErrorContainer
                                } else {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                }
                            )
                        }
                    }

                    // Visibility badge
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = repository.visibility.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase() else it.toString()
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}