package com.instacart.formula

object ChildMessageWithParentStateChange {
    fun formula() = HasChildFormula(MessageFormula()) { state ->
        MessageFormula.Input(
            messageHandler = onEvent<Int> {
                transition(state + 1)
            }
        )
    }
}
