package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Snapshot
import com.instacart.formula.StatelessFormula
import com.instacart.formula.types.OnDataActionFormula

class StreamInputFormula : StatelessFormula<Int, Unit>()  {
    val messages = mutableListOf<Int>()

    private val onDataActionFormula = OnDataActionFormula()

    override fun Snapshot<Int, Unit>.evaluate(): Evaluation<Unit> {
        context.child(
            formula = onDataActionFormula,
            input = OnDataActionFormula.Input(
                data = input,
                onData = context.onEvent {
                    transition {
                        messages.add(it)
                    }
                }
            )
        )

        return Evaluation(output = Unit)
    }
}
