package com.instacart.formula.types

import com.instacart.formula.Action
import com.instacart.formula.Evaluation
import com.instacart.formula.Snapshot
import com.instacart.formula.StatelessFormula

/**
 * On data formula starts an action from [Input.data] and invokes a parent listener
 * on each data change.
 */
class OnDataActionFormula : StatelessFormula<OnDataActionFormula.Input, Unit>() {

    data class Input(
        val data: Int,
        val onData: (Int) -> Unit,
    )

    override fun Snapshot<Input, Unit>.evaluate(): Evaluation<Unit> {
        return Evaluation(
            output = Unit,
            actions = context.actions {
                Action.onData(input.data).onEvent {
                    transition { input.onData(it) }
                }
            }
        )
    }
}