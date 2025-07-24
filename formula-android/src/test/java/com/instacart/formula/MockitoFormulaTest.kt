package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.mockito.internal.stubbing.answers.CallsRealMethods
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class MockitoFormulaTest {

    @Test fun mockMaintainsType() {
        val testFormula = ReplacementFormula().implementation
        val formula = mock<MyFormula>(defaultAnswer = CallsRealMethods())
        whenever(formula.implementation).thenReturn(testFormula)
        assertThat(formula.implementation).isEqualTo(testFormula)
        assertThat(formula.type()).isEqualTo(MyFormula::class.java)
    }

    @Test fun mockActionFormula() {
        val testFormula = ReplacementFormula().implementation
        val formula = mock<MyActionFormula>(defaultAnswer = CallsRealMethods())
        whenever(formula.implementation).thenReturn(testFormula)
        assertThat(formula.implementation).isEqualTo(testFormula)
        assertThat(formula.type()).isEqualTo(MyActionFormula::class.java)
    }

    class MyFormula : StatelessFormula<Unit, Unit>() {
        override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Unit> {
            return Evaluation(Unit)
        }
    }

    class MyActionFormula : ActionFormula<Unit, Unit>() {
        override fun initialValue(input: Unit) = Unit

        override fun action(input: Unit): Action<Unit> {
            return Action.onData(Unit)
        }
    }

    class ReplacementFormula : StatelessFormula<Unit, Unit>() {
        override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Unit> {
            return Evaluation(Unit)
        }
    }
}