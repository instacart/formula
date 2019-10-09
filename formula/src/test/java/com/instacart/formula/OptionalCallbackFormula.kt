package com.instacart.formula

class OptionalCallbackFormula : Formula<Unit, OptionalCallbackFormula.State, OptionalCallbackFormula.RenderModel> {
    data class State(
        val callbackEnabled: Boolean = true,
        val state: Int = 0
    )

    data class RenderModel(
        val state: Int,
        val callback: (() -> Unit)?,
        val toggleCallback: () -> Unit
    )

    override fun initialState(input: Unit) = State()

    override fun evaluate(input: Unit, state: State, context: FormulaContext<State>): Evaluation<RenderModel> {
        val callback = if (state.callbackEnabled) {
            context.callback { state.copy(state = state.state + 1).noEffects() }
        } else {
            null
        }

        return Evaluation(
            renderModel = RenderModel(
                state = state.state,
                callback = callback,
                toggleCallback = context.callback {
                    state.copy(callbackEnabled = !state.callbackEnabled).noEffects()
                }
            )
        )
    }
}
