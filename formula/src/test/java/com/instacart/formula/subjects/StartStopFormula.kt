package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Listener
import com.instacart.formula.Snapshot
import com.instacart.formula.Transition
import com.instacart.formula.TransitionContext
import com.instacart.formula.subjects.StartStopFormula.Output
import com.instacart.formula.subjects.StartStopFormula.State
import com.instacart.formula.test.FlowRelay

class StartStopFormula : Formula<Unit, State, Output>() {

    val incrementEvents = FlowRelay()

    data class State(
        val listenForEvents: Boolean = false,
        val count: Int = 0
    )

    data class Output(
        val state: Int,
        val startListening: Listener<Unit>,
        val stopListening: Listener<Unit>,
    )

    override fun initialState(input: Unit): State = State()

    override fun Snapshot<Unit, State>.evaluate(): Evaluation<Output> {
        return Evaluation(
            actions = context.actions {
                if (state.listenForEvents) {
                    incrementEvents.action().onEvent {
                        transition(state.copy(count = state.count + 1))
                    }
                }
            },
            output = Output(
                state = state.count,
                // We need to specify keys since `UpdateListenFlag` type is used two times.
                startListening = context.onEvent("start", UpdateListenFlag(listen = true)),
                stopListening = context.onEvent("stop", UpdateListenFlag(listen = false)),
            )
        )
    }

    private class UpdateListenFlag(val listen: Boolean): Transition<Any, State, Unit> {
        override fun TransitionContext<Any, State>.toResult(event: Unit): Transition.Result<State> {
            return transition(state.copy(listenForEvents = listen))
        }
    }
}
