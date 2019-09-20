package com.instacart.formula

object ChildMessageNoParentStateChange {
    fun formula() = HasChildFormula.create(MessageFormula()) {
        MessageInput(
            messageHandler = eventCallback { none() }
        )
    }
}
