package com.instacart.formula.test

import com.google.common.truth.Truth
import com.instacart.formula.Evaluation
import com.instacart.formula.Snapshot
import com.instacart.formula.StatelessFormula
import org.junit.Test

class TestFormulaObserverTest {

    @Test fun `assertOutput passes if count matches`() {
        val formula = object : StatelessFormula<Int, Int>() {
            override fun Snapshot<Int, Unit>.evaluate(): Evaluation<Int> {
                return Evaluation(input)
            }
        }

        val observer = formula.test()
        observer.input(1)
        observer.assertOutputCount(1)

        observer.input(10)
        observer.assertOutputCount(2)
    }

    @Test fun `assertOutput throws exception if count does not match`() {
        val formula = object : StatelessFormula<Int, Int>() {
            override fun Snapshot<Int, Unit>.evaluate(): Evaluation<Int> {
                return Evaluation(input)
            }
        }

        val observer = formula.test()
        observer.input(1)
        val result = kotlin.runCatching {
            observer.assertOutputCount(5)
        }

        Truth.assertThat(result.exceptionOrNull()).hasMessageThat().contains(
            "Expected: 5, was: 1"
        )
    }

    @Test fun `output throws error if formula is not running`() {
        val formula = object : StatelessFormula<Int, Int>() {
            override fun Snapshot<Int, Unit>.evaluate(): Evaluation<Int> {
                return Evaluation(input)
            }
        }

        val result = runCatching {
            formula.test().output {  }
        }
        Truth.assertThat(result.exceptionOrNull()).hasMessageThat().contains(
            "Formula is not running. Call [TestFormulaObserver.input] to start it."
        )
    }
}