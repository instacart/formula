package com.instacart.formula.fragment

import com.instacart.formula.RenderView
import com.instacart.formula.Renderer

/**
 * [FragmentComponent] defines the way the [FormulaFragment] can interact
 * with outside world. This class has a [RenderView] that will handle state rendering
 * and also provides a way to listen to fragment lifecycle events.
 */
class FragmentComponent<in RenderModel> private constructor(
    val renderView: RenderView<RenderModel>,
    val lifecycleCallbacks: FragmentLifecycleCallback? = null
) {
    companion object {
        fun <T> noOp(): FragmentComponent<T> {
            return create(
                render = {}
            )
        }

        fun <T> create(render: (T) -> Unit): FragmentComponent<T> {
            return create(
                renderView = object : RenderView<T> {
                    override val renderer: Renderer<T> = Renderer.create(render)
                }
            )
        }

        fun <T> create(
            renderView: RenderView<T>,
            lifecycleCallbacks: FragmentLifecycleCallback? = null
        ): FragmentComponent<T> {
            return FragmentComponent(
                renderView = renderView,
                lifecycleCallbacks = lifecycleCallbacks
            )
        }
    }
}
