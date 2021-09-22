package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.FormulaContext
import com.instacart.formula.StatelessFormula

class KeyFormula : StatelessFormula<TestKey, TestOutput>() {

    override fun key(input: TestKey): Any = input

    override fun evaluate(input: TestKey, context: FormulaContext<Unit>): Evaluation<TestOutput> {
        return Evaluation(
            output = TestOutput(
                input = input,
                value = ""
            )
        )
    }
}