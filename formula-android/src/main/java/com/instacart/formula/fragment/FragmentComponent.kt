package com.instacart.formula.fragment

import com.instacart.formula.Renderer
import com.instacart.formula.RenderView

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
        /**
         * A no-op component which does no rendering
         */
        fun <T> noOp(): FragmentComponent<T> {
            return create(
                render = {}
            )
        }

        /**
         * Creates an inline render view with the given render lambda
         * @param render called on each change to the [RenderModel] to render the view
         */
        fun <T> create(
            render: (T) -> Unit
        ): FragmentComponent<T> {
            return create(
                renderView = object : RenderView<T> {
                    override val render: Renderer<T> = Renderer.create(render)
                },
                lifecycleCallbacks = null
            )
        }

        /**
         * Creates an inline render view with the given render lambda
         *
         * @param render called on each change to the [RenderModel] to render the view
         * @param lifecycleCallbacks Fragment lifecycle callbacks.
         */
        fun <T> create(
            render: (T) -> Unit,
            lifecycleCallbacks: FragmentLifecycleCallback
        ): FragmentComponent<T> {
            return create(
                renderView = object : RenderView<T> {
                    override val render: Renderer<T> = Renderer(render)
                },
                lifecycleCallbacks = lifecycleCallbacks
            )
        }

        /**
         * With the given [renderView] and optional [lifecycleCallbacks], provides the needed integration between
         * the fragment and the [io.reactivex.Observable] stream of [RenderModel]s
         * @param renderView the render view which will receive [RenderModel]s
         * @param lifecycleCallbacks optional lifecycle callbacks that correspond to the fragment lifecycle.
         * Only provide if needed
         */
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
