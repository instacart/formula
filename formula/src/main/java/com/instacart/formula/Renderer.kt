package com.instacart.formula

/**
 * A [Renderer] encapsulates how to apply [RenderModel] to a UI interface.
 */
class Renderer<in RenderModel> private constructor(
    private val render: (RenderModel) -> Unit
) {

    companion object {
        fun <T> empty() = create<T> { }

        /**
         * Creates a basic renderer
         */
        fun <State> create(render: (State) -> Unit): Renderer<State> {
            /**
             * memoize prevents unnecessary updates:
             * it caches last state used and only
             * triggers an update if that state has changed
             */
            return Renderer(render = render.memoize())
        }
    }

    fun render(state: RenderModel) = render.invoke(state)
}
