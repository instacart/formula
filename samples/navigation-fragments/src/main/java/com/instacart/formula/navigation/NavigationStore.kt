package com.instacart.formula.navigation

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class NavigationStore {
    private val _state = MutableStateFlow(NavigationState())
    private val _counterIncrements = MutableSharedFlow<Int>(replay = 0, extraBufferCapacity = Int.MAX_VALUE, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    val state: StateFlow<NavigationState> = _state.asStateFlow()
    val counterIncrements: SharedFlow<Int> = _counterIncrements.asSharedFlow()

    fun onEvent(event: NavigationEvent) {
        when (event) {
            is NavigationEvent.NavigateToFragment -> {
                val currentState = _state.value
                val newState = currentState.navigateToFragment(event.fragmentId)
                _state.value = newState
            }

            is NavigationEvent.NavigateBack -> {
                val currentState = _state.value
                val newState = currentState.navigateBack()
                _state.value = newState
            }

            is NavigationEvent.IncrementCounter -> {
                _counterIncrements.tryEmit(event.fragmentId)
            }
        }
    }

    fun getCurrentState(): NavigationState = _state.value
}