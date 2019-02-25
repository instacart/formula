package com.instacart.formula.fragment

import com.instacart.formula.RenderView
import com.instacart.formula.Renderer

/**
 * [FragmentComponent] defines the way the [com.instacart.formula.FormulaFragment] can interact
 * with outside world. This class has a [RenderView] that will handle state rendering
 * and also provides a way to listen to fragment lifecycle events.
 */
class FragmentComponent<in RenderModel> private constructor(
    val mviView: RenderView<RenderModel>,
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
                mviView = object : RenderView<T> {
                    override val renderer: Renderer<T> = Renderer.create(render)
                }
            )
        }

        fun <T> create(
            mviView: RenderView<T>,
            lifecycleCallbacks: FragmentLifecycleCallback? = null
        ): FragmentComponent<T> {
            return FragmentComponent(
                mviView = mviView,
                lifecycleCallbacks = lifecycleCallbacks
            )
        }
    }
}
