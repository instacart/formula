package com.instacart.formula

object ChildMessageWithParentStateChange {
    fun formula() = HasChildFormula(MessageFormula()) { state ->
        MessageFormula.Input(
            messageHandler = eventCallback {
                Transition(state + 1)
            }
        )
    }
}
