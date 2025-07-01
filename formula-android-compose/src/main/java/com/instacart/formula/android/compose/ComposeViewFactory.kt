package com.instacart.formula.android.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.instacart.formula.android.FeatureView
import com.instacart.formula.android.ViewFactory
import kotlinx.coroutines.flow.MutableStateFlow

abstract class ComposeViewFactory<RenderModel : Any> : ViewFactory<RenderModel> {

    final override fun create(params: ViewFactory.Params): FeatureView<RenderModel> {
        val view = ComposeView(params.context)
        // Based-on: https://developer.android.com/develop/ui/compose/migrate/interoperability-apis/compose-in-views#compose-in-fragments
        view.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

        val outputRelay = MutableStateFlow(
            value = initialModel(),
        )
        view.setContent {
            val model = outputRelay.collectAsState().value
            if (model != null) {
                Content(model)
            }
        }
        return FeatureView(
            view = view,
            setOutput = outputRelay::tryEmit,
            lifecycleCallbacks = null,
        )
    }

    open fun initialModel(): RenderModel? {
        return null
    }

    @Composable
    abstract fun Content(model: RenderModel)
}