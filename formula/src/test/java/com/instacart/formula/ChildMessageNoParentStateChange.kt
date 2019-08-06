package com.instacart.formula

object ChildMessageNoParentStateChange {
    fun formula() = HasChildFormulaV2(MessageFormula()) {
        MessageFormula.Input(
            messageHandler = eventCallback { none() }
        )
    }
}
