package com.instacart.formula.subjects

object MultipleChildEvents {
    fun formula() = HasChildFormula(MessageFormula()) {
        MessageFormula.Input(messageHandler = onEvent<Int> {
            transition(it)
        })
    }
}
