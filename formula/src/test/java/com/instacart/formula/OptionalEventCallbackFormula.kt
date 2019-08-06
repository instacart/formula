package com.instacart.formula

class OptionalEventCallbackFormula : Formula<Unit, OptionalEventCallbackFormula.State, Unit, OptionalEventCallbackFormula.RenderModel> {
    data class State(
        val callbackEnabled: Boolean = true,
        val state: Int = 0
    )

    data class RenderModel(
        val state: Int,
        val callback: ((Int) -> Unit)?,
        val toggleCallback: () -> Unit
    )

    override fun initialState(input: Unit) = State()

    override fun evaluate(input: Unit, state: State, context: FormulaContext<State, Unit>): Evaluation<RenderModel> {
        return Evaluation(
            renderModel = RenderModel(
                state = state.state,
                callback = context.optionalEventCallback(state.callbackEnabled) {
                    state.copy(state = it).transition()
                },
                toggleCallback = context.callback {
                    state.copy(callbackEnabled = !state.callbackEnabled).transition()
                }
            )
        )
    }
}
