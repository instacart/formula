package com.instacart.formula

import io.reactivex.Flowable

/**
 * Defines a factory for [RenderLoop]
 */
interface RenderLoopFormula<Input, State, Effect, RenderModel> : Formula<Input, RenderModel> {

    fun createRenderLoop(input: Input): RenderLoop<State, Effect, RenderModel>

    override fun state(input: Input): Flowable<RenderModel> {
        val loop = createRenderLoop(input)
        return loop.createRenderModelStream()
    }
}
