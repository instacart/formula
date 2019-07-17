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
                callback = if (state.callbackEnabled) {
                    context.eventCallback("my callback") {
                        state.copy(state = it).transition()
                    }
                } else {
                    null
                },
                toggleCallback = context.callback("toggle") {
                    state.copy(callbackEnabled = !state.callbackEnabled).transition()
                }
            )
        )
    }
}
