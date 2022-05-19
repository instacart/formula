package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Listener
import com.instacart.formula.Snapshot
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

    override fun Snapshot<Unit, List<State>>.evaluate(): Evaluation<Output> {
        return Evaluation(
            output = Output(
                events = state,
                onStateTransition = context.onEvent {
                    transition(state.plus(State.INTERNAL)) {
                        relay.triggerEvent()
                    }
                }
            ),
            actions = context.actions {
                relay.action().onEvent {
                    transition(state.plus(State.EXTERNAL))
                }
            }
        )
    }
}