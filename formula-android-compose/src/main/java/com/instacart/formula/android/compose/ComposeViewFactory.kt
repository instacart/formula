package com.instacart.formula.android.compose

import android.os.SystemClock
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rxjava3.subscribeAsState
import androidx.compose.ui.platform.ComposeView
import com.instacart.formula.android.FeatureView
import com.instacart.formula.android.ViewFactory


abstract class ComposeViewFactory<RenderModel> : ViewFactory<RenderModel> {

    override fun create(inflater: LayoutInflater, container: ViewGroup?): FeatureView<RenderModel> {
        val view = ComposeView(inflater.context)
        var firstRender = true
        return FeatureView(
            view = view,
            bind = { state ->
                view.setContent {
                    val model = state.observable.subscribeAsState(null).value
                    if (model != null) {
                        val start = SystemClock.uptimeMillis()
                        Content(model)
                        val end = SystemClock.uptimeMillis()
                        state.environment.eventListener?.onRendered(
                            fragmentId = state.fragmentId,
                            durationInMillis = end - start,
                        )

                        if (firstRender) {
                            firstRender = false
                            state.environment.eventListener?.onFirstModelRendered(
                                fragmentId = state.fragmentId,
                                durationInMillis = end - state.initialized,
                            )
                        }
                    }
                }
                null
            }
        )
    }

    @Composable
    abstract fun Content(model: RenderModel)
}