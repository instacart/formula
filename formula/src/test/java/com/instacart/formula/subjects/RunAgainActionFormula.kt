package com.instacart.formula.subjects

import com.instacart.formula.Action
import com.instacart.formula.DelegateAction
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Listener
import com.instacart.formula.Snapshot
import com.instacart.formula.cancelPrevious
import com.instacart.formula.runAgain
import com.instacart.formula.subjects.RunAgainActionFormula.Output
import com.instacart.formula.subjects.RunAgainActionFormula.State

class RunAgainActionFormula : Formula<Unit, State, Output>() {
    data class State(
        val action: Action<Unit> = Action.onData(Unit),
        val actionExecuted: Int = 0,
        val nullableAction: Action<Unit>? = null,
        val nullableActionExecuted: Int = 0,
        val customAction: CustomAction? = null,
        val customActionExecuted: Int = 0,
    )

    data class Output(
        val actionExecuted: Int,
        val nullableActionExecuted: Int,
        val customActionExecuted: Int,
        val runNullableActionAgain: Listener<Unit>,
        val runActionAgain: Listener<Unit>,
        val runCustomAction: Listener<Unit>,
    )

    class CustomAction(
        val actionNumber: Int,
        action: Action<Unit>,
    ): DelegateAction<Unit>(action)

    override fun initialState(input: Unit): State {
        return State()
    }

    override fun Snapshot<Unit, State>.evaluate(): Evaluation<Output> {
        return Evaluation(
            output = Output(
                actionExecuted = state.actionExecuted,
                nullableActionExecuted = state.nullableActionExecuted,
                customActionExecuted = state.customActionExecuted,
                runNullableActionAgain = context.onEvent {
                    val newState = state.copy(
                        nullableAction = Action
                            .onData(Unit)
                            .cancelPrevious(state.nullableAction)
                    )
                    transition(newState)
                },
                runActionAgain = context.onEvent {
                    val newState = state.copy(
                        action = state.action.runAgain()
                    )
                    transition(newState)
                },
                runCustomAction = context.onEvent {
                    val newState = state.copy(
                        customAction = CustomAction(
                            actionNumber = state.customActionExecuted.inc(),
                            action = Action.onData(Unit).cancelPrevious(state.customAction),
                        )
                    )
                    transition(newState)
                },
            ),
            actions = context.actions {
                state.action.onEvent {
                    val newState = state.copy(
                        actionExecuted = state.actionExecuted.inc()
                    )
                    transition(newState)
                }

                state.nullableAction?.onEvent {
                    val newState = state.copy(
                        nullableActionExecuted = state.nullableActionExecuted.inc()
                    )
                    transition(newState)
                }

                val customAction = state.customAction
                customAction?.onEvent {
                    val newState = state.copy(
                        customActionExecuted = customAction.actionNumber,
                    )
                    transition(newState)
                }
            }
        )
    }
}