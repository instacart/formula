package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Listener
import com.instacart.formula.Snapshot

class MessageFormula : Formula<MessageFormula.Input, Int, MessageFormula.Output>() {

    data class Input(val messageHandler: (Int) -> Unit)

    class Output(
        val state: Int,
        val triggerMessage: Listener<Unit>,
        val incrementAndMessage: Listener<Unit>,
    )

    override fun initialState(input: Input): Int = 0

    override fun Snapshot<Input, Int>.evaluate(): Evaluation<Output> {
        return Evaluation(
            output = Output(
                state = state,
                triggerMessage = context.onEvent {
                    transition {
                        input.messageHandler(state)
                    }
                },
                incrementAndMessage = context.onEvent {
                    val newState = state + 1
                    transition(newState) {
                        input.messageHandler(newState)
                    }
                }
            )
        )
    }
}
