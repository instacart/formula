package com.instacart.formula

import io.reactivex.Flowable

/**
 * The Formula interface defines a protocol for render model management.
 *
 * @param Input - data + callbacks passed by parent to the view model to help construct the RenderModel stream
 * @param RenderModel - is data class that defines how a particular view component should be rendered
 *
 * Ex:
 * ```
 * class MyFeatureFormula @Inject constructor(val someUseCase: SomeUseCase) : Formula<MyFeatureFormula.Input, MyFeatureRenderModel> {
 *   data class Input(val id: String, val onSomeActionTaken: (Action) -> Unit)
 *
 *   fun state(input: Input): Flowable<MyFeatureRenderModel> {
 *     return state().map { state ->
 *       createRenderModel(input, state)
 *     }
 *   }
 * }
 * ```
 */
interface Formula<Input, RenderModel> {

    fun state(input: Input): Flowable<RenderModel>
}
