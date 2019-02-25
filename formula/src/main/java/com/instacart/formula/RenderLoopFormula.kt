package com.instacart.formula

import com.instacart.client.mvi.ICRenderLoop
import io.reactivex.Flowable

/**
 * Defines a factory for [ICRenderLoop]
 */
interface RenderLoopFormula<Input, State, Effect, RenderModel> :
    Formula<Input, RenderModel> {

    fun createRenderLoop(input: Input): ICRenderLoop<State, Effect, RenderModel>

    override fun state(input: Input): Flowable<RenderModel> {
        val loop = createRenderLoop(input)
        return loop.createRenderModelStream()
    }
}
