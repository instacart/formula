package com.instacart.client.mvi

import io.reactivex.Flowable

/**
 * Defines a factory for [ICRenderLoop]
 */
interface ICRenderLoopProvider<Input, State, Effect, RenderModel> : ICViewModel<Input, RenderModel> {

    fun createRenderLoop(input: Input): ICRenderLoop<State, Effect, RenderModel>

    override fun state(input: Input): Flowable<RenderModel> {
        val loop = createRenderLoop(input)
        return loop.createRenderModelStream()
    }
}
