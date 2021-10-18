package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Snapshot
import com.instacart.formula.StatelessFormula

class KeyFormula : StatelessFormula<TestKey, TestOutput>() {

    override fun key(input: TestKey): Any = input

    override fun Snapshot<TestKey, Unit>.evaluate(): Evaluation<TestOutput> {
        return Evaluation(
            output = TestOutput(
                input = input,
                value = ""
            )
        )
    }
}