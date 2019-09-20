package com.instacart.formula

object ChildStateResetAfterToggle {
    fun formula() = OptionalChildFormula.create(MessageFormula()) {
        MessageInput(messageHandler = eventCallback { none() })
    }
}
