package com.instacart.formula.subjects

object ChildMessageNoParentStateChange {
    fun formula() = HasChildFormula(MessageFormula()) {
        MessageFormula.Input(
            messageHandler = onEvent<Int> { none() }
        )
    }
}
