package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Snapshot
import com.instacart.formula.StatelessFormula
import com.instacart.formula.Stream

class RemovingTerminateStreamSendsNoMessagesFormula : StatelessFormula<RemovingTerminateStreamSendsNoMessagesFormula.Input, Unit>() {

    data class Input(
        val onTerminate: (() -> Unit)? = null
    )

    override fun Snapshot<Input, Unit>.evaluate(): Evaluation<Unit> {
        return Evaluation(
            output = Unit,
            updates = context.updates {
                val onTerminate = input.onTerminate
                if (onTerminate != null) {
                    events(Stream.onTerminate()) {
                        transition(onTerminate)
                    }
                }
            }
        )
    }
}
