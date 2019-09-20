package com.instacart.formula

import com.instacart.formula.utils.TestUtils

object OptionalCallbackFormula {
    data class State(
        val callbackEnabled: Boolean = true,
        val state: Int = 0
    )

    data class RenderModel(
        val state: Int,
        val callback: (() -> Unit)?,
        val toggleCallback: () -> Unit
    )

    fun create() = TestUtils.create(State()) { state, context ->
        val callback = if (state.callbackEnabled) {
            context.callback { state.copy(state = state.state + 1).noMessages() }
        } else {
            null
        }

        Evaluation(
            renderModel = RenderModel(
                state = state.state,
                callback = callback,
                toggleCallback = context.callback {
                    state.copy(callbackEnabled = !state.callbackEnabled).noMessages()
                }
            )
        )
    }
}
