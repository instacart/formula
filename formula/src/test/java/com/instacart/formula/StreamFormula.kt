package com.instacart.formula

class StreamFormula : Formula<Unit, StreamFormula.State, StreamFormula.Output> {

    val incrementEvents = IncrementRelay()

    data class State(
        val listenForEvents: Boolean = false,
        val count: Int = 0
    )

    class Output(
        val state: Int,
        val startListening: () -> Unit,
        val stopListening: () -> Unit
    )

    override fun initialState(input: Unit): State = State()

    override fun evaluate(
        input: Unit,
        state: State,
        context: FormulaContext<State>
    ): Evaluation<Output> {
        return Evaluation(
            updates = context.updates {
                if (state.listenForEvents) {
                    events(incrementEvents.stream()) {
                        transition(state.copy(count = state.count + 1))
                    }
                }
            },
            output = Output(
                state = state.count,
                startListening = context.callback {
                    transition(state.copy(listenForEvents = true))
                },
                stopListening = context.callback {
                    transition(state.copy(listenForEvents = false))
                }
            )
        )
    }
}
