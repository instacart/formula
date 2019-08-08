package com.instacart.formula

/**
 * This interface defines how render model is created from a state object. Some things that are done in the generator:
 * 1. Listeners should be added to your RenderModel here. State objects should not have listeners.
 * 2. Simplify your booleans and properties here. If button state depends on multiple properties in the state object, combine those properties here.
 * 3. For expensive state child property transformations, you can add caching here.
 *
 * Ex:
 * ```
 * class MyRenderModelGenerator(
 *   private val onUpSelected: () -> Unit
 * ): RenderModelGenerator<MyState, MyRenderModel> {
 *
 *   fun toRenderModel(state: MyState): MyRenderModel {
 *     return MyRenderModel(
 *       isSaveEnabled = state.event.isData() && state.isMyStateValid,
 *       onUpSelected = onUpSelected
 *     )
 *   }
 * }
 * ```
 */
interface RenderModelGenerator<State, RenderModel> {
    companion object {
        fun <State, RenderModel> create(
            convert: (State) -> RenderModel
        ): RenderModelGenerator<State, RenderModel> {
            return object : RenderModelGenerator<State, RenderModel> {
                override fun toRenderModel(state: State): RenderModel {
                    return convert(state)
                }
            }
        }
    }

    fun toRenderModel(state: State): RenderModel
}
