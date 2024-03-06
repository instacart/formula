package com.instacart.formula.types

import com.instacart.formula.Action
import com.instacart.formula.Evaluation
import com.instacart.formula.Snapshot
import com.instacart.formula.StatelessFormula
import com.instacart.formula.Transition

/**
 * On data formula starts an action from [Input.data] and invokes a parent listener
 * on each data change.
 */
class OnDataActionFormula(
    private val executionType: Transition.ExecutionType? = null,
) : StatelessFormula<OnDataActionFormula.Input, Unit>() {

    data class Input(
        val data: Int,
        val onData: (Int) -> Unit,
    )

    override fun Snapshot<Input, Unit>.evaluate(): Evaluation<Unit> {
        return Evaluation(
            output = Unit,
            actions = context.actions {
                val onData = Action.onData(input.data)
                if (executionType == null) {
                    onData.onEvent {
                        transition { input.onData(it) }
                    }
                } else {
                    onData.onEventWithExecutionType(executionType) {
                        transition { input.onData(it) }
                    }
                }
            }
        )
    }
}