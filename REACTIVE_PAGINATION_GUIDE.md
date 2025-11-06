# Reactive Pagination Pattern Guide

This guide explains the reactive pagination pattern implemented in the GitHub App. This pattern
provides:

- **Reactive UI**: Database changes automatically update the UI
- **Network-first strategy**: Always fetch fresh data while showing cached data
- **Proper pagination**: Load more data incrementally
- **Offline support**: Cached data available when offline

## Architecture Overview

### Data Flow

```
Network API → Repository → Database → Flow → UI
                ↓                       ↑
           Insert Data              Observe Changes
```

1. **UI observes** a Flow from the database
2. **UI triggers** network calls (loadPage, refresh)
3. **Network data** is saved to database
4. **Database changes** automatically update the Flow
5. **UI receives** updated data reactively

## Implementation Details

### 1. Database Layer (DAO)

```kotlin
@Dao
interface RepositoryDao {
    // Reactive query - returns Flow that updates automatically
    @Query("SELECT * FROM repositories WHERE page <= :maxPage ORDER BY page ASC, indexInPage ASC")
    fun getRepositoriesUpToPage(maxPage: Int): Flow<List<RepositoryEntity>>
    
    // Insert new data - triggers Flow updates
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepositories(repositories: List<RepositoryEntity>)
}
```

**Key points:**

- Each repository has a `page` and `indexInPage` field for ordering
- `getRepositoriesUpToPage()` returns a Flow that emits whenever data changes
- When you insert data, all observers automatically get notified

### 2. Repository Layer

```kotlin
class GitHubRepositoryImpl {
    // Returns reactive Flow - UI always observes this
    override fun observeRepositories(maxPage: Int): Flow<List<Repository>> {
        return dao.getRepositoriesUpToPage(maxPage).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    // Loads page from network and saves to DB
    override suspend fun loadPage(page: Int, perPage: Int) {
        val networkRepos = api.getRepositories(page, perPage)
        val entities = networkRepos.mapIndexed { index, repo ->
            repo.toEntity(page = page, indexInPage = index)
        }
        dao.insertRepositories(entities) // This triggers Flow update
    }
}
```

**Key points:**

- `observeRepositories()` provides the reactive stream
- `loadPage()` fetches from network and updates database
- Database insertion automatically notifies all Flow observers

### 3. ViewModel Layer

```kotlin
class RepositoriesViewModel {
    @Composable
    override fun uiState(...): RepositoriesUiState {
        var repositories by remember { mutableStateOf<List<Repository>>(emptyList()) }
        var currentPage by remember { mutableIntStateOf(1) }
        
        // Observe DB - automatically updates when data changes
        LaunchedEffect(currentPage) {
            useCase.observeRepositories(currentPage).collect { repos ->
                repositories = repos
            }
        }
        
        // Load initial data
        LaunchedEffect(Unit) {
            if (repositories.isEmpty()) {
                useCase.loadPage(1, perPage) // Triggers DB update → Flow emits
            }
            
            events.collect { event ->
                when (event) {
                    LoadMore -> {
                        useCase.loadPage(currentPage + 1, perPage)
                        currentPage++
                    }
                    Refresh -> {
                        useCase.refresh(perPage)
                        currentPage = 1
                    }
                }
            }
        }
    }
}
```

**Key points:**

- UI observes `observeRepositories(currentPage)` Flow
- When `currentPage` changes, we observe more data
- Network calls (`loadPage`) trigger database updates
- Database updates automatically flow to UI

## Pagination Strategy

### Loading More Data

1. User scrolls to bottom
2. UI sends `LoadMore` event
3. ViewModel calls `loadPage(nextPage)`
4. Repository fetches from network
5. Data inserted to database with `page = nextPage`
6. ViewModel increments `currentPage`
7. `observeRepositories(currentPage)` includes new page
8. UI automatically updates with new items

### Refreshing Data

1. User pulls to refresh
2. UI sends `Refresh` event
3. ViewModel calls `refresh()`
4. Repository clears database
5. Repository fetches page 1 from network
6. Fresh data inserted to database
7. Flow emits new data
8. UI shows fresh content

## Benefits

### 1. Automatic UI Updates

No need to manually update UI state - Flow handles it:

```kotlin
// Data automatically updates when DB changes
repositories.collect { repos ->
    // UI updates automatically
}
```

### 2. Offline Support

Database cache is always available:

```kotlin
// UI shows cached data immediately
// Network call happens in background
// UI updates when fresh data arrives
```

### 3. Single Source of Truth

Database is the only source of truth:

```kotlin
// Always read from DB
observeRepositories() // Returns Flow<List<Repository>>

// Always write to DB
loadPage() // Fetches network → saves to DB
```

### 4. Simple Pagination

Track current page and observe up to that page:

```kotlin
var currentPage = 1
observeRepositories(maxPage = currentPage) // Shows pages 1..currentPage

// Load more
loadPage(page = currentPage + 1)
currentPage++
// Flow automatically includes new page
```

## Error Handling

Network errors don't break the UI:

```kotlin
try {
    useCase.loadPage(page, perPage)
} catch (e: Exception) {
    // Show error message
    // Cached data still visible via Flow
}
```

## Testing Benefits

Easy to test each layer independently:

```kotlin
// Test Repository
val flow = repository.observeRepositories(maxPage = 1)
repository.loadPage(1, 30)
flow.first() // Assert data

// Test ViewModel
// Mock useCase.observeRepositories() to return test Flow
// Verify UI state updates correctly
```

## Common Patterns

### Initial Load

```kotlin
LaunchedEffect(Unit) {
    if (repositories.isEmpty()) {
        isLoading = true
        try {
            useCase.loadPage(1, perPage)
        } finally {
            isLoading = false
        }
    }
}
```

### Load More

```kotlin
if (!isLoadingMore && hasMoreData) {
    isLoadingMore = true
    try {
        useCase.loadPage(currentPage + 1, perPage)
        currentPage++
    } finally {
        isLoadingMore = false
    }
}
```

### Pull to Refresh

```kotlin
isRefreshing = true
try {
    useCase.refresh(perPage)
    currentPage = 1
} finally {
    isRefreshing = false
}
```

## Migration Checklist

If migrating from imperative to reactive pattern:

- [ ] Add `page` and `indexInPage` fields to entity
- [ ] Add `getRepositoriesUpToPage(maxPage)` Flow query to DAO
- [ ] Change repository to return Flow instead of List
- [ ] Separate "observe" and "load" operations
- [ ] Update ViewModel to collect Flow
- [ ] Fire network calls in background, don't wait for result
- [ ] Remove manual state updates - Flow handles them
- [ ] Update UI to render from Flow data

## Performance Considerations

### Database Triggers

- Room automatically triggers Flow updates on INSERT/UPDATE/DELETE
- Only affected observers receive updates
- Efficient - no manual change tracking needed

### Memory

- Flow only holds current page data in memory
- Old pages remain in database but not in memory
- Use `maxPage` to limit how much data to observe

### Network

- Fire network calls without blocking UI
- Show cached data immediately
- Update UI when fresh data arrives
- Handle errors gracefully

## Conclusion

This reactive pagination pattern provides:

- ✅ Automatic UI updates
- ✅ Offline-first architecture
- ✅ Clean separation of concerns
- ✅ Simple pagination logic
- ✅ Excellent testability
- ✅ Predictable data flow

The key insight: **Always observe from database, always write to database, let Flow handle the rest.
**
