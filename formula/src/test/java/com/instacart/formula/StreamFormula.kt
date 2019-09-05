package com.instacart.formula

class StreamFormula : Formula<Unit, StreamFormula.State, StreamFormula.RenderModel> {

    val incrementEvents = IncrementRelay()

    data class State(
        val listenForEvents: Boolean = false,
        val count: Int = 0
    )

    class RenderModel(
        val state: Int,
        val startListening: () -> Unit,
        val stopListening: () -> Unit
    )

    override fun initialState(input: Unit): State = State()

    override fun evaluate(
        input: Unit,
        state: State,
        context: FormulaContext<State>
    ): Evaluation<RenderModel> {
        return Evaluation(
            updates = context.updates {
                if (state.listenForEvents) {
                    events(incrementEvents.stream()) {
                        state.copy(count = state.count + 1).noMessages()
                    }
                }
            },
            renderModel = RenderModel(
                state = state.count,
                startListening = context.callback {
                    state.copy(listenForEvents = true).noMessages()
                },
                stopListening = context.callback {
                    state.copy(listenForEvents = false).noMessages()
                }
            )
        )
    }
}
