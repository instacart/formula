package com.instacart.formula

import com.instacart.formula.StartStopFormula.Output
import com.instacart.formula.StartStopFormula.State
import com.instacart.formula.test.TestableRuntime

class StartStopFormula(runtime: TestableRuntime) : Formula<Unit, State, Output> {

    val incrementEvents = runtime.newIncrementRelay()

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
                startListening = context.onEvent {
                    transition(state.copy(listenForEvents = true))
                },
                stopListening = context.onEvent {
                    transition(state.copy(listenForEvents = false))
                }
            )
        )
    }
}
