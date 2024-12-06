package com.instacart.formula.android.compose

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rxjava3.subscribeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.instacart.formula.android.FeatureView
import com.instacart.formula.android.ViewFactory
import com.jakewharton.rxrelay3.BehaviorRelay


abstract class ComposeViewFactory<RenderModel : Any> : ViewFactory<RenderModel> {

    override fun create(params: ViewFactory.Params): FeatureView<RenderModel> {
        val view = ComposeView(params.context)
        // Based-on: https://developer.android.com/develop/ui/compose/migrate/interoperability-apis/compose-in-views#compose-in-fragments
        view.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

        val outputRelay = BehaviorRelay.create<RenderModel>()
        view.setContent {
            val model = outputRelay.subscribeAsState(null).value
            if (model != null) {
                Content(model)
            }
        }
        return FeatureView(
            view = view,
            setOutput = outputRelay::accept,
            lifecycleCallbacks = null,
        )
    }

    @Composable
    abstract fun Content(model: RenderModel)
}