package com.instacart.formula.navigation

import com.jakewharton.rxrelay3.BehaviorRelay
import io.reactivex.rxjava3.core.Observable

class NavigationStore {
    private val stateRelay = BehaviorRelay.createDefault(NavigationState())

    val state: Observable<NavigationState> = stateRelay.distinctUntilChanged()

    fun onEvent(event: NavigationEvent) {
        val currentState = stateRelay.value ?: NavigationState()
        val newState = when (event) {
            is NavigationEvent.NavigateToFragment -> currentState.navigateToFragment(event.fragmentId)
            is NavigationEvent.NavigateBack -> currentState.navigateBack()
            is NavigationEvent.IncrementCounter -> currentState.incrementCounter(event.fragmentId)
        }
        stateRelay.accept(newState)
    }

    fun getCurrentState(): NavigationState = stateRelay.value ?: NavigationState()
}