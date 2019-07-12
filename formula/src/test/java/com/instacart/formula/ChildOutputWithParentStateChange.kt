package com.instacart.formula

object ChildOutputWithParentStateChange {
    fun formula() = HasChildFormula(OutputFormula(), onChildOutput = { state, output ->
        Transition(state + 1, output)
    })
}
