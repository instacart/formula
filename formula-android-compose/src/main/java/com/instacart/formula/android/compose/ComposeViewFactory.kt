package com.instacart.formula.android.compose

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rxjava3.subscribeAsState
import androidx.compose.ui.platform.ComposeView
import com.instacart.formula.android.FeatureView
import com.instacart.formula.android.ViewFactory
import com.jakewharton.rxrelay3.BehaviorRelay


abstract class ComposeViewFactory<RenderModel : Any> : ViewFactory<RenderModel> {

    override fun create(inflater: LayoutInflater, container: ViewGroup?): FeatureView<RenderModel> {
        val view = ComposeView(inflater.context)
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
        )
    }

    @Composable
    abstract fun Content(model: RenderModel)
}