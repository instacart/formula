package com.instacart.formula.subjects

import com.instacart.formula.Action
import com.instacart.formula.Evaluation
import com.instacart.formula.Snapshot
import com.instacart.formula.StatelessFormula

class StreamInputFormula : StatelessFormula<Int, Unit>()  {
    val messages = mutableListOf<Int>()

    override fun Snapshot<Int, Unit>.evaluate(): Evaluation<Unit> {
        return Evaluation(
            output = Unit,
            actions = context.actions {
                Action.onData(input).onEvent {
                    transition { messages.add(it) }
                }
            }
        )
    }
}
