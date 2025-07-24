package com.instacart.formula.navigation

import com.jakewharton.rxrelay3.BehaviorRelay
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable

class NavigationStore {
    private val stateRelay = BehaviorRelay.createDefault(NavigationState())
    private val counterIncrementRelay = PublishRelay.create<Int>()

    val state: Observable<NavigationState> = stateRelay.distinctUntilChanged()
    val counterIncrements: Observable<Int> = counterIncrementRelay

    fun onEvent(event: NavigationEvent) {
        when (event) {
            is NavigationEvent.NavigateToFragment -> {
                val currentState = stateRelay.value ?: NavigationState()
                val newState = currentState.navigateToFragment(event.fragmentId)
                stateRelay.accept(newState)
            }

            is NavigationEvent.NavigateBack -> {
                val currentState = stateRelay.value ?: NavigationState()
                val newState = currentState.navigateBack()
                stateRelay.accept(newState)
            }

            is NavigationEvent.IncrementCounter -> {
                counterIncrementRelay.accept(event.fragmentId)
            }
        }
    }

    fun getCurrentState(): NavigationState = stateRelay.value ?: NavigationState()
}