package com.instacart.client.mvi.state

import io.reactivex.Flowable
import io.reactivex.disposables.Disposable

/**
 * Defines parameters necessary to construct a state management loop. It encapsulates:
 * 1. State changes over time
 * 2. Effect emission.
 *
 * [initialState] - A starting state object before any transformations are performed.
 * [reducers] - A stream of reducer functions that transform the state and emit effects.
 * [initialEffects] - Initial effects to be emitted.
 * [onEffect] - A callback that gets triggered when new effects are produced by the reducers.
 * [onStateChange] - A callback that gets triggered every time the state changes.
 */
class ICStateLoop<State, Effect>(
    val initialState: State,
    val reducers: Flowable<NextReducer<State, Effect>>,
    val initialEffects: Set<Effect> = emptySet(),
    val onEffect: (Effect) -> Unit = {},
    val onStateChange: (State) -> Unit = {}
) {
    /**
     * Combines all the parameters and creates a state redux stream.
     */
    fun createLoop(): Flowable<State> {
        return ICLoop.createLoop(initialState, reducers, initialEffects, onEffect).doOnNext(onStateChange)
    }

    /**
     * Creates a loop and subscribes to it. Doesn't handle any
     * errors.
     */
    fun unsafeRun(): Disposable {
        return createLoop().subscribe()
    }
}