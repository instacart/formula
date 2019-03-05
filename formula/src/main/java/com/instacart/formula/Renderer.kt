package com.instacart.formula

/**
 * A [Renderer] encapsulates how to apply [RenderModel] to a UI interface. It avoids
 * duplicate updates. Use the [create] method to construct a [Renderer]
 */
class Renderer<in RenderModel> private constructor(
    private val renderFunction: (RenderModel) -> Unit
) {

    companion object {

        /**
         * Creates an empty renderer
         */
        fun <T> empty() = create<T> { }

        /**
         * Creates a basic renderer
         */
        fun <State> create(render: (State) -> Unit): Renderer<State> {
            return Renderer(renderFunction = render)
        }
    }

    private var pending: RenderModel? = null
    private var inProgress: Boolean = false
    private var last: RenderModel? = null

    /**
     * Render the passed render model, first checking to see if a render is already in progress, or the passed state
     * is equivalent to the last state.
     */
    fun render(state: RenderModel) {
        if (inProgress) {
            pending = state
            return
        }

        inProgress = true

        val local = last

        if (local == null || local != state) {
            renderFunction(state)
        }

        last = state
        inProgress = false

        // Check if there is a pending update and execute it.
        val localPending = pending
        pending = null
        if (localPending != null) {
            render(localPending)
        }
    }
}
