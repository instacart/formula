package com.instacart.formula

object ChildMessageWithParentStateChange {
    fun formula() = HasChildFormula(MessageFormula()) { state ->
        MessageFormula.Input(
            messageHandler = eventCallback {
                transition(state + 1)
            }
        )
    }
}
