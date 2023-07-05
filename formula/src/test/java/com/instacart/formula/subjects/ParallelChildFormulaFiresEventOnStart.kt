package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Snapshot
import com.instacart.formula.StatelessFormula
import com.instacart.formula.types.IncrementFormula
import com.instacart.formula.types.OnStartActionFormula

object ParallelChildFormulaFiresEventOnStart {

    fun formula(eventNumber: Int): Parent {
        return Parent(
            first = IncrementFormula(),
            second = OnStartActionFormula(eventNumber),
        )
    }

    class Parent(
        val first: IncrementFormula,
        val second: OnStartActionFormula,
    ) : StatelessFormula<Unit, Int>() {
        override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Int> {
            val firstOutput = context.child(first)
            context.child(second, OnStartActionFormula.Input(onAction = firstOutput.onIncrement))
            return Evaluation(firstOutput.value)
        }
    }
}