package com.instacart.formula

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
