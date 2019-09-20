package com.instacart.formula

import com.instacart.formula.test.test
import com.instacart.formula.utils.TestUtils

object OptionalEventCallbackFormula {
    data class State(
        val callbackEnabled: Boolean = true,
        val state: Int = 0
    )

    data class RenderModel(
        val state: Int,
        val callback: ((Int) -> Unit)?,
        val toggleCallback: () -> Unit
    )

    fun test() = formula().test()

    private fun formula() = TestUtils.create(State()) { state, context ->
        val callback = if (state.callbackEnabled) {
            context.eventCallback<Int> {
                state.copy(state = it).noMessages()
            }
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
