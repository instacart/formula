package com.instacart.formula.android.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.instacart.formula.android.FeatureView
import com.instacart.formula.android.ViewFactory

abstract class ComposeViewFactory<RenderModel : Any> : ViewFactory<RenderModel> {

    final override fun create(params: ViewFactory.Params): FeatureView<RenderModel> {
        val view = ComposeView(params.context)
        // Based-on: https://developer.android.com/develop/ui/compose/migrate/interoperability-apis/compose-in-views#compose-in-fragments
        view.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

        val outputState = mutableStateOf(initialModel())
        view.setContent {
            outputState.value?.let { model ->
                Content(model)
            }
        }
        return FeatureView(
            view = view,
            setOutput = { newValue ->
                outputState.value = newValue
            },
            lifecycleCallbacks = null,
        )
    }

    open fun initialModel(): RenderModel? {
        return null
    }

    @Composable
    abstract fun Content(model: RenderModel)
}