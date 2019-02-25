package com.instacart.formula

/**
 * Responsible for taking render model
 * and rendering the view using it.
 */
class Renderer<in State> private constructor(
    private val render: (State) -> Unit
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

    fun render(state: State) = render.invoke(state)
}
