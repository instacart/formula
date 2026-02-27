package com.instacart.formula.subjects

import com.instacart.formula.Action
import com.instacart.formula.Evaluation
import com.instacart.formula.Snapshot
import com.instacart.formula.StatelessFormula

class RemovingTerminateStreamSendsMessageFormula : StatelessFormula<RemovingTerminateStreamSendsMessageFormula.Input, Unit>() {

    data class Input(
        val onTerminate: (() -> Unit)? = null
    )

    override fun Snapshot<Input, Unit>.evaluate(): Evaluation<Unit> {
        return Evaluation(
            output = Unit,
            actions = context.actions {
                val onTerminate = input.onTerminate
                if (onTerminate != null) {
                    Action.onTerminate().onEvent {
                        transition(onTerminate)
                    }
                }
            }
        )
    }
}
