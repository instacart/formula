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
        fun <RenderModel> create(render: (RenderModel) -> Unit): Renderer<RenderModel> {
            return Renderer(renderFunction = render)
        }
    }

    private enum class State {
        NOT_INITIALIZED,
        INITIALIZED,
        UPDATE_IN_PROGRESS
    }

    private var state: State = State.NOT_INITIALIZED
    private var pending: (() -> Unit)? = null
    private var last: RenderModel? = null

    /**
     * Render the passed render model, first checking to see if a render is already in progress, or the passed render model
     * is equivalent to the last render model.
     */
    fun render(renderModel: RenderModel) {
        val lastState = this.state
        if (lastState == State.UPDATE_IN_PROGRESS) {
            pending = { render(renderModel) }
            return
        }

        state = State.UPDATE_IN_PROGRESS

        val local = last
        last = renderModel

        try {
            if (lastState == State.NOT_INITIALIZED || local != renderModel) {
                renderFunction(renderModel)
            }
            state = State.INITIALIZED
        } catch (e: Exception) {
            // Reset state
            last = local
            state = lastState
            // Rethrow the exception
            throw e
        }

        // Check if there is a pending update and execute it.
        val localPending = pending
        pending = null
        localPending?.invoke()
    }
}
