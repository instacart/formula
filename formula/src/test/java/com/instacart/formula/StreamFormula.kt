package com.instacart.formula

import com.instacart.formula.utils.TestUtils

data class StreamState(
    val listenForEvents: Boolean = false,
    val count: Int = 0
)

class StreamRenderModel(
    val state: Int,
    val startListening: () -> Unit,
    val stopListening: () -> Unit
)

fun StreamFormula(incrementEvents: IncrementRelay) = TestUtils.create(StreamState()) { state, context ->
    Evaluation(
        updates = context.updates {
            if (state.listenForEvents) {
                events(incrementEvents.stream()) {
                    state.copy(count = state.count + 1).noMessages()
                }
            }
        },
        renderModel = StreamRenderModel(
            state = state.count,
            startListening = context.callback {
                state.copy(listenForEvents = true).noMessages()
            },
            stopListening = context.callback {
                state.copy(listenForEvents = false).noMessages()
            }
        )
    )
}
