package com.instacart.formula.android.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.instacart.formula.android.ViewFeatureView
import com.instacart.formula.android.ViewFactory

/**
 * A factory that supports both Fragment-based rendering (via [ViewFactory]) and
 * Nav3 Compose rendering (via [ComposeRenderFactory]).
 *
 * For Fragment-based rendering, this creates a [ViewFeatureView] with a [ComposeView] wrapper.
 * For Nav3 rendering, the [Content] function is called directly without any View wrapper.
 *
 * Use this class when you need to support both Fragment and Nav3 navigation modes.
 * For Nav3-only features, consider using [ComposeRenderFactory] directly for better efficiency.
 *
 * Example usage:
 * ```
 * class MyViewFactory : ComposeViewFactory<MyRenderModel>() {
 *     @Composable
 *     override fun Content(model: MyRenderModel) {
 *         MyScreen(model)
 *     }
 * }
 * ```
 *
 * @see ComposeRenderFactory for Nav3-only features
 * @see ViewFactory for View-based features
 */
abstract class ComposeViewFactory<RenderModel : Any> : ViewFactory<RenderModel>, ComposeRenderFactory<RenderModel> {

    /**
     * Creates a [ViewFeatureView] for Fragment-based rendering.
     * This wraps the Compose content in a [ComposeView].
     */
    final override fun create(params: ViewFactory.Params): ViewFeatureView<RenderModel> {
        val view = ComposeView(params.context)
        // Based-on: https://developer.android.com/develop/ui/compose/migrate/interoperability-apis/compose-in-views#compose-in-fragments
        view.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

        val outputState = mutableStateOf(initialModel())
        view.setContent {
            val model = outputState.value
            if (model != null) {
                Content(model)
            }
        }
        return ViewFeatureView(
            view = view,
            setOutput = { outputState.value = it },
            lifecycleCallbacks = null,
        )
    }

    /**
     * Optional initial model to display before the first state emission.
     * Override this to provide a loading or placeholder state.
     */
    open fun initialModel(): RenderModel? {
        return null
    }

    /**
     * The Composable content to render for the given model.
     */
    @Composable
    abstract override fun Content(model: RenderModel)
}
