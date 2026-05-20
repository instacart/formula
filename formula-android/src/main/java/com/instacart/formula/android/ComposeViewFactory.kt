package com.instacart.formula.android

import androidx.compose.runtime.Composable

/**
 * [ViewFactory] subtype for Compose-rendered routes. Subclasses implement
 * [Content] to render the route's render model, and may optionally override
 * [initialModel] to supply a model rendered before the first state emission.
 *
 * ```
 * class MyViewFactory : ComposeViewFactory<MyRenderModel>() {
 *     @Composable
 *     override fun Content(model: MyRenderModel) {
 *         MyScreen(model)
 *     }
 * }
 * ```
 */
abstract class ComposeViewFactory<RenderModel : Any> : ViewFactory<RenderModel> {

    @Composable
    abstract fun Content(model: RenderModel)

    /** Optional initial model rendered before the first state emission. Defaults to null. */
    open fun initialModel(): RenderModel? = null
}
