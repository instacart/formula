package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Test
import org.mockito.internal.stubbing.answers.CallsRealMethods

class MockitoFormulaTest {

    @Test fun mockMaintainsType() {
        val testFormula = ReplacementFormula().implementation()
        val formula = mock<MyFormula>(defaultAnswer = CallsRealMethods())
        whenever(formula.implementation()).thenReturn(testFormula)
        assertThat(formula.implementation()).isEqualTo(testFormula)
        assertThat(formula.type()).isEqualTo(MyFormula::class)
    }

    class MyFormula : StatelessFormula<Unit, Unit>() {
        override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Unit> {
            return Evaluation(Unit)
        }
    }

    class ReplacementFormula : StatelessFormula<Unit, Unit>() {
        override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Unit> {
            return Evaluation(Unit)
        }
    }
}