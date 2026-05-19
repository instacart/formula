package com.instacart.testutils.android

import com.instacart.formula.android.FeatureView
import com.instacart.formula.android.ViewFactory

/**
 * Test [ViewFactory] that records each render call without producing UI.
 * The [onRender] callback is invoked from a `@Composable` body, so it fires
 * on each non-skipped recomposition (Compose may skip recompositions when the
 * input model is structurally equal to the previous one).
 */
class TestViewFactory<RenderModel>(
    private val onRender: (RenderModel) -> Unit = {},
) : ViewFactory<RenderModel> {
    override fun create(params: ViewFactory.Params): FeatureView<RenderModel> {
        return FeatureView(
            content = { model -> onRender(model) },
        )
    }
}
