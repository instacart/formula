package com.instacart.formula

class OptionalEventCallbackFormula : Formula<Unit, OptionalEventCallbackFormula.State, OptionalEventCallbackFormula.Output> {
    data class State(
        val callbackEnabled: Boolean = true,
        val state: Int = 0
    )

    data class Output(
        val state: Int,
        val callback: ((Int) -> Unit)?,
        val toggleCallback: () -> Unit
    )

    override fun initialState(input: Unit) = State()

    override fun evaluate(input: Unit, state: State, context: FormulaContext<State>): Evaluation<Output> {
        val callback = if (state.callbackEnabled) {
            context.eventCallback<Int> {
                state.copy(state = it).noEffects()
            }
        } else {
            null
        }

        return Evaluation(
            output = Output(
                state = state.state,
                callback = callback,
                toggleCallback = context.callback {
                    state.copy(callbackEnabled = !state.callbackEnabled).noEffects()
                }
            )
        )
    }
}
