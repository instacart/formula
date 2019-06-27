package com.instacart.formula

import io.reactivex.Observable

/**
 * The RxFormula interface defines a protocol for render model management.
 *
 * Ex:
 * ```
 * class MyFeatureFormula @Inject constructor(val someUseCase: SomeUseCase) : RxFormula<MyFeatureFormula.Input, MyFeatureRenderModel> {
 *   data class Input(val id: String, val onSomeActionTaken: (Action) -> Unit)
 *
 *   override fun state(input: Input): Observable<MyFeatureRenderModel> {
 *     return state().map { state ->
 *       createRenderModel(input, state)
 *     }
 *   }
 * }
 * ```
 * See also [RenderFormula]
 *
 * @param Input data + callbacks passed by parent to the view model to help construct the RenderModel stream
 * @param RenderModel is data class that defines how a particular view component should be rendered
 */
interface RxFormula<in Input, RenderModel> {

    /**
     * Creates the render model stream, using the given input
     * @param input The input (callbacks, initial values, etc.) needed to create a stream of render models
     */
    fun state(input: Input): Observable<RenderModel>
}
