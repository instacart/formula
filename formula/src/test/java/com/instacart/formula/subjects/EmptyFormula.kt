package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Snapshot
import com.instacart.formula.StatelessFormula

class EmptyFormula: StatelessFormula<Unit, Unit>() {
    override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Unit> {
        return Evaluation(output = Unit)
    }
}