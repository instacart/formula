package com.instacart.formula.subjects

object ChildStateResetAfterToggle {
    fun formula() = OptionalChildFormula(MessageFormula()) {
        MessageFormula.Input(messageHandler = onEvent<Int> { none() })
    }
}
