package com.instacart.formula

import com.instacart.formula.utils.TestUtils

data class MessageInput(val messageHandler: (Int) -> Unit)

data class MessageRenderModel(
    val state: Int,
    val triggerMessage: () -> Unit,
    val incrementAndMessage: () -> Unit
)

fun MessageFormula() = TestUtils.create(0) { input: MessageInput, state, context ->
    Evaluation(
        renderModel = MessageRenderModel(
            state = state,
            triggerMessage = context.callback {
                state.withMessages {
                    message(input.messageHandler, state)
                }
            },
            incrementAndMessage = context.callback {
                val newState = state + 1
                newState.withMessages {
                    message(input.messageHandler, newState)
                }
            }
        )
    )
}
