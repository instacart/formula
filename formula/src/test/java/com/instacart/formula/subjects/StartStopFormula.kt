package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.FormulaContext
import com.instacart.formula.Listener
import com.instacart.formula.Transition
import com.instacart.formula.TransitionContext
import com.instacart.formula.subjects.StartStopFormula.Output
import com.instacart.formula.subjects.StartStopFormula.State
import com.instacart.formula.test.TestableRuntime

class StartStopFormula(runtime: TestableRuntime) : Formula<Unit, State, Output>() {

    val incrementEvents = runtime.newRelay()

    data class State(
        val listenForEvents: Boolean = false,
        val count: Int = 0
    )

    class Output(
        val state: Int,
        val startListening: Listener<Unit>,
        val stopListening: Listener<Unit>,
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
                // We need to specify keys since `UpdateListenFlag` type is used two times.
                startListening = context.onEvent("start", UpdateListenFlag(listen = true)),
                stopListening = context.onEvent("stop", UpdateListenFlag(listen = false)),
            )
        )
    }

    private class UpdateListenFlag(val listen: Boolean): Transition<State, Unit> {
        override fun TransitionContext<State>.toResult(event: Unit): Transition.Result<State> {
            return transition(state.copy(listenForEvents = listen))
        }
    }
}
