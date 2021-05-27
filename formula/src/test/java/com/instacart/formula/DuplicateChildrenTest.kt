package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.internal.Try
import com.instacart.formula.test.test
import org.junit.Test
import kotlin.IllegalStateException

class DuplicateChildrenTest {

    @Test fun `adding duplicate child throws an exception`() {
        val error = Try { ParentFormula().test(Unit) }.errorOrNull()?.cause
        assertThat(error).isInstanceOf(IllegalStateException::class.java)
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
