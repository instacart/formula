package com.instacart.formula

object MultipleChildEvents {
    fun formula() = HasChildFormula(MessageFormula()) {
        MessageFormula.Input(messageHandler = onEvent<Int> {
            transition(it)
        })
    }
}
