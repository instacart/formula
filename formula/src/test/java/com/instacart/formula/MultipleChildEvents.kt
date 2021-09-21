package com.instacart.formula

import com.instacart.formula.subjects.HasChildFormula

object MultipleChildEvents {
    fun formula() = HasChildFormula(MessageFormula()) {
        MessageFormula.Input(messageHandler = onEvent<Int> {
            transition(it)
        })
    }
}
