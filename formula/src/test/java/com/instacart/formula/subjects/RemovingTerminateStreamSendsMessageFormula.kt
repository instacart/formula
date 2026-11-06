package com.instacart.formula.subjects

import com.instacart.formula.Action
import com.instacart.formula.Snapshot
import com.instacart.formula.StatelessFormula

class RemovingTerminateStreamSendsMessageFormula : StatelessFormula<RemovingTerminateStreamSendsMessageFormula.Input, Unit>() {

    data class Input(
        val onTerminate: (() -> Unit)? = null
    )

    override fun Snapshot<Input, Unit>.evaluate(): Unit {
        notifyOnTerminate()
        return Unit
    }

    private fun Snapshot<Input, Unit>.notifyOnTerminate() {
        val onTerminate = input.onTerminate
        if (onTerminate != null) {
            Action.onTerminate().onEvent {
                transition(onTerminate)
            }
        }
    }
}
