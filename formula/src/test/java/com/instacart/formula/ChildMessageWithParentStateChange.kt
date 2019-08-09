package com.instacart.formula

object ChildMessageWithParentStateChange {
    fun formula() = HasChildFormulaV2(MessageFormula()) { state ->
        MessageFormula.Input(
            messageHandler = eventCallback {
                Transition(state + 1)
            }
        )
    }
}
