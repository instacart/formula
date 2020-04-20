package com.instacart.formula

/**
 * Render class defines how to apply a [RenderModel] to a UI interface. It avoids
 * duplicate updates. Use the [create] method to construct a [Render]
 *
 * ```
 * val renderText = Render<String> { text ->
 *   myView.setText(text)
 * }
 * renderText("first")
 * renderText("two")
 * renderText("three")
 * ```
 */
class Render<in RenderModel> private constructor(
    private val renderFunction: (RenderModel) -> Unit
) : (RenderModel) -> Unit {

    companion object {

        /**
         * Creates a render function that does nothing.
         */
        fun <T> empty() = create<T> { }

        /**
         * Creates a render function.
         */
        operator fun <RenderModel> invoke(render: (RenderModel) -> Unit): Render<RenderModel> {
            return Render(renderFunction = render)
        }

        /**
         * Creates a render function.
         */
        fun <RenderModel> create(render: (RenderModel) -> Unit): Render<RenderModel> {
            return Render(render)
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


    override fun invoke(renderModel: RenderModel) {
        val lastState = this.state
        if (lastState == State.UPDATE_IN_PROGRESS) {
            pending = { invoke(renderModel) }
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
