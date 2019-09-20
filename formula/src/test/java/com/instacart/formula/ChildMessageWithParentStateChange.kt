package com.instacart.formula

object ChildMessageWithParentStateChange {
    fun formula() = HasChildFormula.create(MessageFormula()) { state ->
        MessageInput(
            messageHandler = eventCallback {
                Transition(state + 1)
            }
        )
    }
}
