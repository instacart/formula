package com.instacart.formula

class RemovingStreamSendsCancelMessageFormula : StatelessFormula<RemovingStreamSendsCancelMessageFormula.Input, Unit>() {

    data class Input(
        val onCancel: (() -> Unit)? = null
    )

    override fun evaluate(input: Input, context: FormulaContext<Unit>): Evaluation<Unit> {
        return Evaluation(
            renderModel = Unit,
            updates = context.updates {
                if (input.onCancel != null) {
                    events(Stream.onCancel()) {
                        message(input.onCancel)
                    }
                }
            }
        )
    }
}
