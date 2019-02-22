package com.instacart.client.mvi.state

import io.reactivex.Flowable

@Deprecated("use ICStateLoop or ICRenderLoop")
object ICLoop {

    private fun <Model, Effect> createLoop(
        initialModel: Model,
        reducers: Flowable<NextReducer<Model, Effect>>,
        initialEffects: Set<Effect> = emptySet()
    ): Flowable<ICNext<Model, Effect>> {
        return reducers
            .scan(ICNext(initialModel, initialEffects)) { state, reducer ->
                reducer(state.state)
            }
    }

    @Deprecated("use ICStateLoop or ICRenderLoop")
    fun <Model, Effect> createLoop(
        initialModel: Model,
        reducers: Flowable<NextReducer<Model, Effect>>,
        initialEffects: Set<Effect> = emptySet(),
        onEffect: (Effect) -> Unit = {}
    ): Flowable<Model> {
        return createLoop(initialModel, reducers, initialEffects)
            .doOnNext {
                it.effects.forEach(onEffect)
            }
            .map { it.state }
            .distinctUntilChanged()
    }
}
