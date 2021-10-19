package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Snapshot
import com.instacart.formula.StatelessFormula
import com.instacart.formula.Stream

class StreamInputFormula : StatelessFormula<Int, Unit>()  {
    val messages = mutableListOf<Int>()

    override fun Snapshot<Int, Unit>.evaluate(): Evaluation<Unit> {
        return Evaluation(
            output = Unit,
            updates = context.updates {
                Stream.onData(input).onEvent {
                    transition { messages.add(it) }
                }
            }
        )
    }
}
