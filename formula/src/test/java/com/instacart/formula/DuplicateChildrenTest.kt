package com.instacart.formula

import org.junit.Test
import java.lang.IllegalStateException

class DuplicateChildrenTest {

    @Test fun `adding duplicate child throws an exception`() {
        ParentFormula().start().test().assertError {
            it is IllegalStateException
        }
    }

    class ParentFormula : StatelessFormula<Unit, List<Unit>>() {

        override fun evaluate(input: Unit, context: FormulaContext<Unit>): Evaluation<List<Unit>> {
            return Evaluation(
                output = listOf(1, 2, 3).map {
                    context.child(ChildFormula())
                }
            )
        }
    }

    class ChildFormula: StatelessFormula<Unit, Unit>() {
        override fun evaluate(input: Unit, context: FormulaContext<Unit>): Evaluation<Unit> {
            return Evaluation(
                output = Unit
            )
        }
    }
}
