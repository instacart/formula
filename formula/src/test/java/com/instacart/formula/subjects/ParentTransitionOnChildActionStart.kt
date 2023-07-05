package com.instacart.formula.subjects

import com.instacart.formula.types.OnInitActionFormula

object ParentTransitionOnChildActionStart {

    fun formula(eventNumber: Int) = run {
        val child = OnInitActionFormula(eventNumber)
        HasChildFormula(child) {
            OnInitActionFormula.Input(
                onAction = callback {
                    transition(state + 1)
                }
            )
        }
    }
}