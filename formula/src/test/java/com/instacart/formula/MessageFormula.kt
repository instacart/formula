package com.instacart.formula

class MessageFormula : Formula<MessageFormula.Input, Int, MessageFormula.RenderModel> {

    data class Input(val messageHandler: (Int) -> Unit)

    class RenderModel(
        val state: Int,
        val triggerMessage: () -> Unit,
        val incrementAndMessage: () -> Unit
    )

    override fun initialState(input: Input): Int = 0

    override fun evaluate(input: Input, state: Int, context: FormulaContext<Int>): Evaluation<RenderModel> {
        return Evaluation(
            renderModel = RenderModel(
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
}
