package com.instacart.formula.android

import androidx.compose.runtime.Composable

/**
 * Convenience [ViewFactory] base class for Compose-rendered routes.
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

    final override fun create(params: ViewFactory.Params): FeatureView<RenderModel> {
        return FeatureView(
            content = { model -> Content(model) },
            initialModel = initialModel(),
        )
    }

    /** Optional initial model rendered before the first state emission. Defaults to null. */
    open fun initialModel(): RenderModel? {
        return null
    }

    @Composable
    abstract fun Content(model: RenderModel)
}
