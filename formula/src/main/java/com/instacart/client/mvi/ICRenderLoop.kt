package com.instacart.client.mvi

import com.instacart.client.mvi.state.ICStateLoop
import com.instacart.client.mvi.state.NextReducer
import io.reactivex.Flowable

/**
 * [ICRenderLoop] combines [ICStateLoop] and [ICRenderModelGenerator] to define how [RenderModel] changes over time.
 *
 * [stateLoop] - Defines how state changes over time.
 * [renderModelGenerator] - Responsible for converting state object to a render model object.
 */
class ICRenderLoop<State, Effect, RenderModel> private constructor(
    val stateLoop: ICStateLoop<State, Effect>,
    val renderModelGenerator: ICRenderModelGenerator<State, RenderModel>,
    val onStateChange: (State) -> Unit = {}
) {
    companion object {
        /**
         * Instantiates [ICRenderLoop]
         *
         * [initialState] - A starting state object before any transformations are performed.
         * [reducers] - A stream of reducer functions that transform the state and emit effects.
         * [renderModelGenerator] - responsible for converting state object to a render model object.
         * [initialEffects] - Initial effects to be emitted.
         * [onEffect] - A callback that gets triggered when new effects are produced by the reducers.
         * [onStateChange] - A callback that gets triggered every time the state changes.
         */
        operator fun <State, Effect, RenderModel> invoke(
            initialState: State,
            reducers: Flowable<NextReducer<State, Effect>>,
            renderModelGenerator: ICRenderModelGenerator<State, RenderModel>,
            initialEffects: Set<Effect> = emptySet(),
            onEffect: (Effect) -> Unit = {},
            onStateChange: (State) -> Unit = {}
        ): ICRenderLoop<State, Effect, RenderModel> {
            return ICRenderLoop(
                stateLoop = ICStateLoop(
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

    fun createRenderModelStream(): Flowable<RenderModel> {
        return stateLoop.createLoop().map(renderModelGenerator::toRenderModel)
    }
}
