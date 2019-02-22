package com.instacart.client.mvi

/**
 * [ICMviFragmentComponent] defines the way the [ICMviFragment] can interact
 * with outside world. This class has a [ICMviView] that will handle state rendering
 * and also provides a way to listen to fragment lifecycle events.
 */
class ICMviFragmentComponent<in RenderModel> private constructor(
    val mviView: ICMviView<RenderModel>,
    val lifecycleCallbacks: ICFragmentLifecycleCallback? = null
) {
    companion object {
        fun <T> noOp(): ICMviFragmentComponent<T> {
            return create(
                render = {}
            )
        }

        fun <T> create(render: (T) -> Unit): ICMviFragmentComponent<T> {
            return create(
                mviView = object : ICMviView<T> {
                    override val renderer: ICRenderer<T> = ICRenderer.create(render)
                }
            )
        }

        fun <T> create(
            mviView: ICMviView<T>,
            lifecycleCallbacks: ICFragmentLifecycleCallback? = null
        ): ICMviFragmentComponent<T> {
            return ICMviFragmentComponent(
                mviView = mviView,
                lifecycleCallbacks = lifecycleCallbacks
            )
        }
    }
}
