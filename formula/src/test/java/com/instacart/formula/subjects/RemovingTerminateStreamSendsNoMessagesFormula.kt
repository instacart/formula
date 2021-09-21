package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.FormulaContext
import com.instacart.formula.StatelessFormula
import com.instacart.formula.Stream

class RemovingTerminateStreamSendsNoMessagesFormula : StatelessFormula<RemovingTerminateStreamSendsNoMessagesFormula.Input, Unit>() {

    data class Input(
        val onTerminate: (() -> Unit)? = null
    )

    override fun evaluate(input: Input, context: FormulaContext<Unit>): Evaluation<Unit> {
        return Evaluation(
            output = Unit,
            updates = context.updates {
                if (input.onTerminate != null) {
                    events(Stream.onTerminate()) {
                        transition(input.onTerminate)
                    }
                }
            }
        )
    }
}
