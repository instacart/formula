package com.instacart.testutils.android

import androidx.compose.runtime.Composable
import com.instacart.formula.android.ComposeViewFactory

/**
 * Test [ComposeViewFactory] that records each render call without producing UI.
 * The [onRender] callback is invoked from a `@Composable` body, so it fires
 * on each non-skipped recomposition (Compose may skip recompositions when the
 * input model is structurally equal to the previous one).
 */
class TestViewFactory<RenderModel : Any>(
    private val onRender: (RenderModel) -> Unit = {},
) : ComposeViewFactory<RenderModel>() {
    @Composable
    override fun Content(model: RenderModel) {
        onRender(model)
    }
}
