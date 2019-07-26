package com.instacart.formula

class OptionalCallbackFormula : Formula<Unit, OptionalCallbackFormula.State, Unit, OptionalCallbackFormula.RenderModel> {
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

    override fun evaluate(input: Unit, state: State, context: FormulaContext<State, Unit>): Evaluation<RenderModel> {
        return Evaluation(
            renderModel = RenderModel(
                state = state.state,
                callback = context.optionalCallback(state.callbackEnabled) {
                    state.copy(state = state.state + 1).transition()
                },
                toggleCallback = context.callback {
                    state.copy(callbackEnabled = !state.callbackEnabled).transition()
                }
            )
        )
    }
}
