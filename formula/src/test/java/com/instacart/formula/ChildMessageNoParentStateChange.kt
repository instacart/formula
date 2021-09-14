package com.instacart.formula

object ChildMessageNoParentStateChange {
    fun formula() = HasChildFormula(MessageFormula()) {
        MessageFormula.Input(
            messageHandler = onEvent<Int> { none() }
        )
    }
}
