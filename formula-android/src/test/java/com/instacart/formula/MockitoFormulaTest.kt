package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Test

class MockitoFormulaTest {

    @Test fun mockMaintainsType() {
        val testFormula = ReplacementFormula().implementation()
        val formula = spy<MyFormula>()
        whenever(formula.implementation()).thenReturn(testFormula)
        assertThat(formula.implementation()).isEqualTo(testFormula)
        assertThat(formula.type()).isEqualTo(MyFormula::class)
    }

    class MyFormula : StatelessFormula<Unit, Unit>() {
        override fun evaluate(input: Unit, context: FormulaContext<Unit>): Evaluation<Unit> {
            return Evaluation(Unit)
        }
    }

    class ReplacementFormula : StatelessFormula<Unit, Unit>() {
        override fun evaluate(input: Unit, context: FormulaContext<Unit>): Evaluation<Unit> {
            return Evaluation(Unit)
        }
    }
}