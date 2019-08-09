package com.instacart.formula

object MultipleChildEvents {
    fun formula() = HasChildFormulaV2(MessageFormula()) {
        MessageFormula.Input(messageHandler = eventCallback {
            transition(it)
        })
    }
}
