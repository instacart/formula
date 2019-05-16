package com.instacart.formula

import io.reactivex.Observable

/**
 * [RenderLoop] combines [StateLoop] and [RenderModelGenerator] to define how [RenderModel] changes over time.
 *
 * @param stateLoop Defines how state changes over time.
 * @param renderModelGenerator Responsible for converting state object to a render model object.
 */
class RenderLoop<State, Effect, RenderModel> private constructor(
    val stateLoop: StateLoop<State, Effect>,
    val renderModelGenerator: RenderModelGenerator<State, RenderModel>
) {
    companion object {
        /**
         * Instantiates [RenderLoop]
         *
         * @param initialState A starting state object before any transformations are performed.
         * @param reducers A stream of reducer functions that transform the state and emit effects.
         * @param renderModelGenerator responsible for converting state object to a render model object.
         * @param initialEffects Initial effects to be emitted.
         * @param onEffect A callback that gets triggered when new effects are produced by the reducers.
         * @param onStateChange A callback that gets triggered every time the state changes.
         */
        operator fun <State, Effect, RenderModel> invoke(
            initialState: State,
            reducers: Observable<NextReducer<State, Effect>>,
            renderModelGenerator: RenderModelGenerator<State, RenderModel>,
            initialEffects: Set<Effect> = emptySet(),
            onEffect: (Effect) -> Unit = {},
            onStateChange: (State) -> Unit = {}
        ): RenderLoop<State, Effect, RenderModel> {
            return RenderLoop(
                stateLoop = StateLoop(
                    initialState = initialState,
                    reducers = reducers,
                    initialEffects = initialEffects,
                    onEffect = onEffect,
                    onStateChange = onStateChange
                ),
                renderModelGenerator = renderModelGenerator
            )
        }
    }

    fun createRenderModelStream(): Observable<RenderModel> {
        return stateLoop.createLoop().map(renderModelGenerator::toRenderModel)
    }
}
