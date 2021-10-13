package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.FormulaContext
import com.instacart.formula.Listener
import com.instacart.formula.test.TestableRuntime

class StateTransitionTimingFormula(
    runtime: TestableRuntime
): Formula<Unit, List<StateTransitionTimingFormula.State>, StateTransitionTimingFormula.Output>() {

    enum class State {
        INTERNAL,
        EXTERNAL
    }

    data class Output(
        val events: List<State>,
        val onStateTransition: Listener<Unit>,
    )

    private val relay = runtime.newRelay()

    override fun initialState(input: Unit): List<State> = emptyList()

    override fun evaluate(
        input: Unit,
        state: List<State>,
        context: FormulaContext<List<State>>
    ): Evaluation<Output> {
        return Evaluation(
            output = Output(
                events = state,
                onStateTransition = context.onEvent {
                    transition(state.plus(State.INTERNAL)) {
                        relay.triggerEvent()
                    }
                }
            ),
            updates = context.updates {
                relay.stream().onEvent {
                    transition(state.plus(State.EXTERNAL))
                }
            }
        )
    }
}