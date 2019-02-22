package com.instacart.client.mvi

import com.instacart.client.mvi.utils.memoize

/**
 * Responsible for taking render model
 * and rendering the view using it.
 */
class ICRenderer<in State> private constructor(
    private val render: (State) -> Unit
) {

    companion object {
        fun <T> empty() = create<T> { }

        /**
         * Creates a basic renderer
         */
        fun <State> create(render: (State) -> Unit): ICRenderer<State> {
            /**
             * memoize prevents unnecessary updates:
             * it caches last state used and only
             * triggers an update if that state has changed
             */
            return ICRenderer(render = render.memoize())
        }
    }

    fun render(state: State) = render.invoke(state)
}