package com.instacart.formula

class OptionalEventCallbackFormula : Formula<Unit, OptionalEventCallbackFormula.State, OptionalEventCallbackFormula.RenderModel> {
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

    override fun evaluate(input: Unit, state: State, context: FormulaContext<State>): Evaluation<RenderModel> {
        return Evaluation(
            renderModel = RenderModel(
                state = state.state,
                callback = context.optionalEventCallback(state.callbackEnabled) {
                    state.copy(state = it).noMessages()
                },
//                callback = context.optional {
//                    if (state.callbackEnabled) {
//                        context.eventCallback<Int> {
//                            state.copy(state = it).transition()
//                        }
//                    } else {
//                        null
//                    }
//                },
                toggleCallback = context.callback {
                    state.copy(callbackEnabled = !state.callbackEnabled).noMessages()
                }
            )
        )
    }
}
