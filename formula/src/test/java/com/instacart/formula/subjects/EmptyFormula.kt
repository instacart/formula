package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.FormulaContext
import com.instacart.formula.StatelessFormula

class EmptyFormula: StatelessFormula<Unit, Unit>() {
    override fun evaluate(input: Unit, context: FormulaContext<Unit>): Evaluation<Unit> {
        return Evaluation(output = Unit)
    }
}