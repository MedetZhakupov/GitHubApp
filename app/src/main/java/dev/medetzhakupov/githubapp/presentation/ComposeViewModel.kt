package dev.medetzhakupov.githubapp.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow

abstract class ComposeViewModel<UIState, Event, Action>(
    private val recompositionMode: RecompositionMode = RecompositionMode.ContextClock,
    coroutineScope: CoroutineScope? = null,
) : ViewModel() {

    private val scope = coroutineScope ?:
     CoroutineScope(SupervisorJob() + AndroidUiDispatcher.Main)

    private val events = MutableSharedFlow<Event>(extraBufferCapacity = 20)

    private val actionChannel = Channel<Action>()

    val action = actionChannel.receiveAsFlow()

    val ui: StateFlow<UIState> by lazy(LazyThreadSafetyMode.NONE) {
        scope.launchMolecule(recompositionMode) {
            uiState(events, actionChannel)
        }
    }

    fun onEvent(event: Event) {
        if (!events.tryEmit(event)) {
            error("Event buffer overflow")
        }
    }

    @Composable
    protected abstract fun uiState(
        events: Flow<Event>,
        action: Channel<Action>
    ): UIState
}