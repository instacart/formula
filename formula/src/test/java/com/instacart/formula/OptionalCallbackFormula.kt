package com.instacart.formula

class OptionalCallbackFormula : Formula<Unit, OptionalCallbackFormula.State, OptionalCallbackFormula.Output> {
    data class State(
        val callbackEnabled: Boolean = true,
        val state: Int = 0
    )

    data class Output(
        val state: Int,
        val callback: (() -> Unit)?,
        val toggleCallback: () -> Unit
    )

    override fun initialState(input: Unit) = State()

    override fun evaluate(input: Unit, state: State, context: FormulaContext<State>): Evaluation<Output> {
        val callback = if (state.callbackEnabled) {
            context.callback { transition(state.copy(state = state.state + 1)) }
        } else {
            null
        }

        return Evaluation(
            output = Output(
                state = state.state,
                callback = callback,
                toggleCallback = context.callback {
                    transition(state.copy(callbackEnabled = !state.callbackEnabled))
                }
            )
        )
    }
}
