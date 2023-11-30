package com.instacart.formula.android.views

import android.os.SystemClock
import com.instacart.formula.Cancelable
import com.instacart.formula.Renderer
import com.instacart.formula.android.FeatureView
import java.lang.Exception

/**
 * Binds [FeatureView.State] to a [render] function.
 */
internal class FeatureViewBindFunction<RenderModel>(
    private val render: Renderer<RenderModel>
) : (FeatureView.State<RenderModel>) -> Cancelable? {
    override fun invoke(state: FeatureView.State<RenderModel>): Cancelable {
        val environment = state.environment
        val disposable = state.observable.subscribe {
            try {
                val start = SystemClock.uptimeMillis()
                render(it)
                val end = SystemClock.uptimeMillis()
                environment.eventListener?.onRendered(
                    fragmentId = state.fragmentId,
                    durationInMillis = end - start,
                )
            } catch (exception: Exception) {
                environment.onScreenError(state.fragmentId.key, exception)
            }
        }
        return Cancelable {
            disposable.dispose()
        }
    }
}