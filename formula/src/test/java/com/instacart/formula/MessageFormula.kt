package com.instacart.formula

class MessageFormula : Formula<MessageFormula.Input, Int, MessageFormula.Output> {

    data class Input(val messageHandler: (Int) -> Unit)

    class Output(
        val state: Int,
        val triggerMessage: () -> Unit,
        val incrementAndMessage: () -> Unit
    )

    override fun initialState(input: Input): Int = 0

    override fun evaluate(input: Input, state: Int, context: FormulaContext<Int>): Evaluation<Output> {
        return Evaluation(
            output = Output(
                state = state,
                triggerMessage = context.callback {
                    transition {
                        input.messageHandler(state)
                    }
                },
                incrementAndMessage = context.callback {
                    val newState = state + 1
                    transition(newState) {
                        input.messageHandler(newState)
                    }
                }
            )
        )
    }
}
