package com.instacart.formula.subjects

import com.instacart.formula.Action
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Listener
import com.instacart.formula.Snapshot
import com.instacart.formula.runAgain
import com.instacart.formula.subjects.RunAgainActionFormula.Output
import com.instacart.formula.subjects.RunAgainActionFormula.State

class RunAgainActionFormula : Formula<Unit, State, Output>() {
    data class State(
        val action: Action<Unit> = Action.onData(Unit),
        val actionExecuted: Int = 0,
        val nullableAction: Action<Unit>? = null,
        val nullableActionExecuted: Int = 0,
    )

    data class Output(
        val actionExecuted: Int,
        val nullableActionExecuted: Int,
        val runNullableActionAgain: Listener<Unit>,
        val runActionAgain: Listener<Unit>,
    )

    override fun initialState(input: Unit): State {
        return State()
    }

    override fun Snapshot<Unit, State>.evaluate(): Evaluation<Output> {
        return Evaluation(
            output = Output(
                actionExecuted = state.actionExecuted,
                nullableActionExecuted = state.nullableActionExecuted,
                runNullableActionAgain = context.onEvent {
                    val newState = state.copy(
                        nullableAction = state.nullableAction.runAgain { Action.onData(Unit) }
                    )
                    transition(newState)
                },
                runActionAgain = context.onEvent {
                    val newState = state.copy(
                        action = state.action.runAgain()
                    )
                    transition(newState)
                }
            ),
            actions = context.actions {
                state.action.onEvent {
                    val newState = state.copy(actionExecuted = state.actionExecuted + 1)
                    transition(newState)
                }

                state.nullableAction?.onEvent {
                    val newState = state.copy(nullableActionExecuted = state.nullableActionExecuted + 1)
                    transition(newState)
                }
            }
        )
    }
}