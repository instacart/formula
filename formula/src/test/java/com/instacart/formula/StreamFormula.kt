package com.instacart.formula

class StreamFormula : Formula<Unit, StreamFormula.State, Unit, StreamFormula.RenderModel> {

    val incrementEvents = IncrementRxStream()

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
        context: FormulaContext<State, Unit>
    ): Evaluation<RenderModel> {
        return Evaluation(
            updates = context.updates {
                if (state.listenForEvents) {
                    events(incrementEvents, onEvent = {
                        transition(state.copy(count = state.count + 1))
                    })
                }
            },
            renderModel = RenderModel(
                state = state.count,
                startListening = context.callback {
                    state.copy(listenForEvents = true).transition()
                },
                stopListening = context.callback {
                    state.copy(listenForEvents = false).transition()
                }
            )
        )
    }
}
