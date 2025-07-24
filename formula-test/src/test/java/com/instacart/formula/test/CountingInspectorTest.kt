package com.instacart.formula.test

import com.google.common.truth.Truth
import com.instacart.formula.Evaluation
import com.instacart.formula.Snapshot
import com.instacart.formula.StatelessFormula
import org.junit.Test

class CountingInspectorTest {

    @Test
    fun `assertEvaluationCount throws exception when count does not match`() {
        val inspector = CountingInspector()
        inspector.assertEvaluationCount(0)

        val result = runCatching { inspector.assertEvaluationCount(5) }
        Truth.assertThat(result.exceptionOrNull()).hasMessageThat().contains(
            "Evaluation count does not match - count: 0, expected: 5"
        )
    }

    @Test
    fun `assertEvaluationCount with types throws exception when count does not match`() {
        val inspector = CountingInspector()
        inspector.assertEvaluationCount(MyFormula::class, 0)
        inspector.onEvaluateFinished(MyFormula::class.java, null, true)
        inspector.assertEvaluationCount(MyFormula::class, 1)

        val result = runCatching { inspector.assertEvaluationCount(MyFormula::class, 5) }
        Truth.assertThat(result.exceptionOrNull()).hasMessageThat().contains(
            "Evaluation count does not match - count: 1, expected: 5"
        )
    }

    @Test
    fun `assertRunCount throws exception when count does not match`() {
        val inspector = CountingInspector()
        inspector.assertRunCount(0)

        val result = runCatching { inspector.assertRunCount(5) }
        Truth.assertThat(result.exceptionOrNull()).hasMessageThat().contains(
            "Run count does not match - count: 0, expected: 5"
        )
    }

    @Test
    fun `assertActionsStarted throws exception when count does not match`() {
        val inspector = CountingInspector()
        inspector.assertActionsStarted(0)

        val result = runCatching { inspector.assertActionsStarted(5) }
        Truth.assertThat(result.exceptionOrNull()).hasMessageThat().contains(
            "Actions started count does not match - count: 0, expected: 5"
        )
    }

    @Test
    fun `assertStateTransitions throws exception when count does not match`() {
        val inspector = CountingInspector()
        inspector.assertStateTransitions(MyFormula::class, 0)
        inspector.onStateChanged(MyFormula::class.java, null, null, null)
        inspector.assertStateTransitions(MyFormula::class, 1)

        val result = runCatching { inspector.assertStateTransitions(MyFormula::class, 5) }
        Truth.assertThat(result.exceptionOrNull()).hasMessageThat().contains(
            "State transition count does not match - count: 1, expected: 5"
        )
    }

    class MyFormula : StatelessFormula<Unit, Unit>() {
        override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Unit> {
            return Evaluation(Unit)
        }
    }
}