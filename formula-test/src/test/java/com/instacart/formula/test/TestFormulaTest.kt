package com.instacart.formula.test

import com.google.common.truth.Truth.assertThat
import junit.framework.Assert.fail
import kotlinx.coroutines.test.runTest
import org.junit.Test

class TestFormulaTest {
    @Test fun `assert running count is zero when formula is not running`() = runTest {
        val formula = TestSimpleFormula()
        formula.implementation.assertRunningCount(0)

        val observer = formula.test(this)
        observer.input(SimpleFormula.Input())
        formula.implementation.assertRunningCount(1)

        observer.dispose()
        formula.implementation.assertRunningCount(0)
    }

    @Test
    fun `assert running count throws an exception when count does not match`() {
        val formula = TestSimpleFormula()
        val result = runCatching {
            formula.implementation.assertRunningCount(5)
        }
        assertThat(result.exceptionOrNull()).hasMessageThat().contains(
            "Expected 5 running formulas, but there were 0 instead"
        )
    }

    @Test fun `emits initial output when subscribed`() = runTest {
        val initialOutput = SimpleFormula.Output(100, "random")
        val formula = TestSimpleFormula(initialOutput)
        formula.test(this).input(SimpleFormula.Input()).output {
            assertThat(this).isEqualTo(initialOutput)
        }
    }

    @Test fun `output throws an exception when no test formula is running`() {
        try {
            val formula = TestSimpleFormula()
            formula.implementation.output(SimpleFormula.Output(0, ""))
            fail("Should not happen")
        } catch (e: Exception) {
            assertThat(e).hasMessageThat().startsWith("Formula is not running")
        }
    }

    @Test fun `output emits new output to the parent`() = runTest {
        val formula = TestSimpleFormula()
        val observer = formula.test(this).input(SimpleFormula.Input())

        val newOutput = SimpleFormula.Output(5, "random")
        formula.implementation.output(newOutput)
        observer.output { assertThat(this).isEqualTo(newOutput) }
    }

    @Test fun `output with key throws an exception when test formula matching key is not running`() = runTest {
        try {
            val newOutput = SimpleFormula.Output(5, "random")

            val formula = TestSimpleFormula()
            val observer = formula.test(this).input(SimpleFormula.Input())
            formula.implementation.output("random-key", newOutput)
            fail("Should not happen")
        } catch (e: Exception) {
            assertThat(e).hasMessageThat().startsWith("Formula for random-key is not running, there are [simple-formula-key] running")
        }
    }

    @Test fun `output with key emits new output to the parent when key matches`() = runTest {
        val newOutput = SimpleFormula.Output(5, "random")

        val formula = TestSimpleFormula()
        val observer = formula.test(this).input(SimpleFormula.Input())
        formula.implementation.output("simple-formula-key", newOutput)
        observer.output { assertThat(this).isEqualTo(newOutput) }
    }

    @Test fun `updateOutput uses previous output and emits new one to the parent`() = runTest {
        val formula = TestSimpleFormula()
        val observer = formula.test(this).input(SimpleFormula.Input())

        formula.implementation.updateOutput { copy(outputId = outputId.inc()) }
        observer.output { assertThat(this).isEqualTo(SimpleFormula.Output(1, "")) }
    }

    @Test fun `updateOutput with key emits a modified output when key matches a running formula`() = runTest {
        val formula = TestSimpleFormula()
        val observer = formula.test(this).input(SimpleFormula.Input())

        formula.implementation.updateOutput("simple-formula-key") { copy(outputId = outputId.inc()) }
        observer.output { assertThat(this).isEqualTo(SimpleFormula.Output(1, "")) }
    }

    @Test fun `updateOutput with key throws an error when there is no running formula matching the key`() = runTest {
        try {
            val formula = TestSimpleFormula()
            val observer = formula.test(this).input(SimpleFormula.Input())

            formula.implementation.updateOutput("random-key") { copy(outputId = outputId.inc()) }
            observer.output { assertThat(this).isEqualTo(SimpleFormula.Output(1, "")) }
            fail()
        } catch (e: Exception) {
            assertThat(e).hasMessageThat().startsWith("Formula for random-key is not running, there are [simple-formula-key] running")
        }
    }

    @Test fun `input() throw an exception when no formulas are running`() {
        try {
            val formula = TestSimpleFormula()
            formula.implementation.input {  }

            fail("Should not happen")
        } catch (e: Exception) {
            assertThat(e).hasMessageThat().startsWith("Formula is not running")
        }
    }

    @Test fun `input() emits the last input provided by the parent`() = runTest {
        val myInput = SimpleFormula.Input("my-input-id")
        val formula = TestSimpleFormula()
        val observer = formula.test(this)

        // Initial input
        observer.input(myInput)
        formula.implementation.input { assertThat(this).isEqualTo(myInput) }

        // Next input
        val nextInput = SimpleFormula.Input("next-input-id")
        observer.input(nextInput)
        formula.implementation.input { assertThat(this).isEqualTo(nextInput) }
    }

    @Test fun `input() throws an error when NO formula that matches the key provided is running`() = runTest {
        try {
            val formula = TestSimpleFormula()
            formula.test(this).input(SimpleFormula.Input())
            formula.implementation.input(key = "random-key") {}

            fail("Should not happen")
        } catch (e: Exception) {
            assertThat(e).hasMessageThat().startsWith("Formula for random-key is not running, there are [simple-formula-key] running")
        }
    }

    @Test fun `input() works as expected when formula that matches the key provided is running`() = runTest {
        val myInput = SimpleFormula.Input()
        val formula = TestSimpleFormula()
        formula.test(this).input(myInput)
        formula.implementation.input(key = "simple-formula-key") {
            assertThat(this).isEqualTo(myInput)
        }
    }

    @Test fun `mostRecentInput returns last input passed by parent`() = runTest {
        val inputA = SimpleFormula.Input("a")
        val inputB = SimpleFormula.Input("b")
        val formula = TestSimpleFormula()
        formula.test(this).input(inputA).input(inputB)

        val mostRecentInput = formula.implementation.mostRecentInput()
        assertThat(mostRecentInput).isEqualTo(inputB)
    }

    @Test fun `mostRecentInputs returns all inputs parent passed`() = runTest {
        val inputA = SimpleFormula.Input("a")
        val inputB = SimpleFormula.Input("b")
        val formula = TestSimpleFormula()
        formula.test(this).input(inputA).input(inputB)

        assertThat(formula.implementation.mostRecentInputs()).containsExactly(
            inputA, inputB
        ).inOrder()
    }

    @Test fun `inputByKey returns last input passed by parent`() = runTest {
        val inputA = SimpleFormula.Input("a")
        val inputB = SimpleFormula.Input("b")
        val formula = TestSimpleFormula()
        formula.test(this).input(inputA).input(inputB)

        val inputByKey = formula.implementation.inputByKey(SimpleFormula.CUSTOM_KEY)
        assertThat(inputByKey).isEqualTo(inputB)
    }

    @Test fun `inputsByKey returns all inputs parent passed`() = runTest {
        val inputA = SimpleFormula.Input("a")
        val inputB = SimpleFormula.Input("b")
        val formula = TestSimpleFormula()
        formula.test(this).input(inputA).input(inputB)

        assertThat(formula.implementation.inputsByKey(SimpleFormula.CUSTOM_KEY)).containsExactly(
            inputA, inputB
        ).inOrder()
    }

    @Test fun `default key is null`() {
        val formula = TestSimpleFormula(useCustomKey = false)
        val key = formula.key(SimpleFormula.Input())
        assertThat(key).isNull()
    }

    @Test fun `custom key is applied correctly`() {
        val formula = TestSimpleFormula(useCustomKey = true)
        val key = formula.key(SimpleFormula.Input())
        assertThat(key).isEqualTo(SimpleFormula.CUSTOM_KEY)
    }
}
