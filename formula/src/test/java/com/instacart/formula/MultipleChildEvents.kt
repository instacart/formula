package com.instacart.formula

import com.instacart.formula.Transition.Factory.transition

object MultipleChildEvents {
    fun formula() = HasChildFormula(OutputFormula(), onChildOutput = { state, event ->
        transition(event.state)
    })
}
