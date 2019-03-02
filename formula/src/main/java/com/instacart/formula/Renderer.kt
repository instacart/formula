package com.instacart.formula

/**
 * A [Renderer] encapsulates how to apply [RenderModel] to a UI interface. It avoids
 * duplicate updates.
 */
class Renderer<in RenderModel> private constructor(
    private val renderFunction: (RenderModel) -> Unit
) {

    companion object {
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
