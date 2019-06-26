package com.instacart.formula

import io.reactivex.Observable

/**
 * Defines a factory for [RenderLoop]
 */
interface RenderFormula<in Input, State, Effect, RenderModel> : RxFormula<Input, RenderModel> {

    /**
     * Creates the render loop, using the given input
     * @param input The input (callbacks, initial values, etc.) needed to create a render loop
     */
    fun createRenderLoop(input: Input): RenderLoop<State, Effect, RenderModel>

    override fun state(input: Input): Observable<RenderModel> {
        val loop = createRenderLoop(input)
        return loop.createRenderModelStream()
    }
}
