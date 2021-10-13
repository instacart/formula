package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.FormulaContext
import com.instacart.formula.Listener

class OptionalEventCallbackFormula :
    Formula<Unit, OptionalEventCallbackFormula.State, OptionalEventCallbackFormula.Output>() {
    data class State(
        val callbackEnabled: Boolean = true,
        val state: Int = 0
    )

    data class Output(
        val state: Int,
        val listener: Listener<Int>?,
        val toggleListener: Listener<Unit>
    )

    override fun initialState(input: Unit) = State()

    override fun evaluate(input: Unit, state: State, context: FormulaContext<State>): Evaluation<Output> {
        val callback = if (state.callbackEnabled) {
            context.onEvent<Int> {
                transition(state.copy(state = it))
            }
        } else {
            null
        }

        return Evaluation(
            output = Output(
                state = state.state,
                listener = callback,
                toggleListener = context.onEvent {
                    transition(state.copy(callbackEnabled = !state.callbackEnabled))
                }
            )
        )
    }
}
