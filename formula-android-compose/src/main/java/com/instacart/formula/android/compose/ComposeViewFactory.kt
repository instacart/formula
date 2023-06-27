package com.instacart.formula.android.compose

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rxjava3.subscribeAsState
import androidx.compose.ui.platform.ComposeView
import com.instacart.formula.android.FeatureView
import com.instacart.formula.android.ViewFactory


abstract class ComposeViewFactory<RenderModel : Any> : ViewFactory<RenderModel> {

    override fun create(inflater: LayoutInflater, container: ViewGroup?): FeatureView<RenderModel> {
        val view = ComposeView(inflater.context)
        return FeatureView(
            view = view,
            bind = {
                view.setContent {
                    val model = it.observable.subscribeAsState(null).value
                    if (model != null) {
                        Content(model)
                    }
                }
                null
            }
        )
    }

    @Composable
    abstract fun Content(model: RenderModel)
}