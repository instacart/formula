package com.instacart.formula

object ChildStateResetAfterToggle {
    fun formula() = OptionalChildFormula(MessageFormula()) {
        MessageFormula.Input(messageHandler = onEvent<Int> { none() })
    }
}
