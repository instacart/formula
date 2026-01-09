package com.instacart.formula.android.compose

import androidx.compose.runtime.Composable
import com.instacart.formula.android.RenderFactory

/**
 * Factory for Nav3 Compose rendering.
 *
 * Unlike [ViewFactory][com.instacart.formula.android.ViewFactory] which creates Android Views,
 * this factory renders directly to Compose without any View infrastructure.
 *
 * Example usage:
 * ```
 * class MyNav3Factory : ComposeRenderFactory<MyRenderModel> {
 *     @Composable
 *     override fun Content(model: MyRenderModel) {
 *         MyScreen(model)
 *     }
 * }
 * ```
 *
 * @see RenderFactory for the base interface
 * @see ComposeViewFactory for a factory that supports both Fragment and Nav3 rendering
 */
fun interface ComposeRenderFactory<RenderModel> : RenderFactory<RenderModel> {
    /**
     * Composable function that renders the given [model].
     */
    @Composable
    fun Content(model: RenderModel)
}
