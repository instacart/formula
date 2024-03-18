package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Listener
import com.instacart.formula.Snapshot
import com.instacart.formula.Transition

class OptionalCallbackFormula(
    private val toggleExecutionType: Transition.ExecutionType? = null,
    private val incrementExecutionType: Transition.ExecutionType? = null,
) : Formula<Unit, OptionalCallbackFormula.State, OptionalCallbackFormula.Output>() {
    data class State(
        val callbackEnabled: Boolean = true,
        val state: Int = 0
    )

    data class Output(
        val state: Int,
        val listener: Listener<Unit>?,
        val toggleCallback: Listener<Unit>,
    )

    override fun initialState(input: Unit) = State()

    override fun Snapshot<Unit, State>.evaluate(): Evaluation<Output> {
        val callback = if (state.callbackEnabled) {
            context.onEventWithExecutionType<Unit>(incrementExecutionType) {
                transition(state.copy(state = state.state + 1))
            }
        } else {
            null
        }

        return Evaluation(
            output = Output(
                state = state.state,
                listener = callback,
                toggleCallback = context.onEventWithExecutionType(toggleExecutionType) {
                    transition(state.copy(callbackEnabled = !state.callbackEnabled))
                }
            )
        )
    }
}
