package com.instacart.formula

import com.instacart.formula.subjects.HasChildFormula

object ChildMessageNoParentStateChange {
    fun formula() = HasChildFormula(MessageFormula()) {
        MessageFormula.Input(
            messageHandler = onEvent<Int> { none() }
        )
    }
}
