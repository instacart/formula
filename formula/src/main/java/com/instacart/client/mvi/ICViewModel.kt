package com.instacart.client.mvi

import io.reactivex.Flowable

/**
 * The ICViewModel interface defines a protocol for render model management.
 *
 * @param Input - data + callbacks passed by parent to the view model to help construct the RenderModel stream
 * @param RenderModel - is data class that defines how a particular view component should be rendered
 *
 * Ex:
 * ```
 * class MyFeatureViewModel @Inject constructor(val someUseCase: SomeUseCase) : ICViewModel<MyFeatureViewModel.Input, MyFeatureRenderModel> {
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
interface ICViewModel<Input, RenderModel> {

    fun state(input: Input): Flowable<RenderModel>
}
