package com.instacart.formula

import com.instacart.formula.subjects.HasChildFormula

object ChildMessageWithParentStateChange {
    fun formula() = HasChildFormula(MessageFormula()) { state ->
        MessageFormula.Input(
            messageHandler = onEvent<Int> {
                transition(state + 1)
            }
        )
    }
}
