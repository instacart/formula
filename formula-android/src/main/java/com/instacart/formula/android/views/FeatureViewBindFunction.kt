package com.instacart.formula.android.views

import com.instacart.formula.Cancelable
import com.instacart.formula.Renderer
import com.instacart.formula.android.FeatureView
import java.lang.Exception

/**
 * Binds [FeatureView.State] to a [render] function.
 */
internal class FeatureViewBindFunction<RenderModel : Any>(
    private val render: Renderer<RenderModel>
) : (FeatureView.State<RenderModel>) -> Cancelable? {
    override fun invoke(state: FeatureView.State<RenderModel>): Cancelable {
        val disposable = state.observable.subscribe {
            try {
                render(it)
            } catch (exception: Exception) {
                state.onError(exception)
            }
        }
        return Cancelable {
            disposable.dispose()
        }
    }
}