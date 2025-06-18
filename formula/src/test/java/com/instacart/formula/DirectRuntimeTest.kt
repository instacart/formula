package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.internal.Try
import com.instacart.formula.test.TestEventCallback
import com.instacart.formula.types.InputIdentityFormula
import org.junit.Test

/**
 * [FormulaRuntimeTest] runs both `toObservable` and `toFlow` internally to ensure that both
 * implementations function identically. This test is used to capture various edge-cases
 * within [FormulaRuntime] that are not possible via indirect tests.
 */
class DirectRuntimeTest {

    @Test fun `requireInput will throw illegal state exception if it is null`() {
        val root = InputIdentityFormula<Int>()

        val onOutput = TestEventCallback<Int>()
        val onError = TestEventCallback<Throwable>()
        val runtime = FormulaRuntime(
            formula = root,
            config = RuntimeConfig()
        )
        runtime.setOnOutput(onOutput)
        runtime.setOnError(onError)

        val result = Try {
            runtime.requireInput()
        }
        assertThat(result.errorOrNull()).isInstanceOf(IllegalStateException::class.java)
    }

    @Test fun `requireManager will throw illegal state exception if it is null`() {
        val root = InputIdentityFormula<Int>()

        val onOutput = TestEventCallback<Int>()
        val onError = TestEventCallback<Throwable>()
        val runtime = FormulaRuntime(
            formula = root,
            config = RuntimeConfig()
        )
        runtime.setOnOutput(onOutput)
        runtime.setOnError(onError)

        val result = Try {
            runtime.requireManager()
        }
        assertThat(result.errorOrNull()).isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `input change when runtime is terminated does nothing`() {
        val root = InputIdentityFormula<Int>()

        val onOutput = TestEventCallback<Int>()
        val onError = TestEventCallback<Throwable>()
        val runtime = FormulaRuntime(
            formula = root,
            config = RuntimeConfig()
        )
        runtime.setOnOutput(onOutput)
        runtime.setOnError(onError)

        runtime.onInput(0)
        runtime.terminate()
        runtime.onInput(1)

        assertThat(onOutput.values()).containsExactly(0).inOrder()
    }

    @Test fun `it is safe to call terminate before first input initializes formula`() {
        val root = InputIdentityFormula<Int>()

        val onOutput = TestEventCallback<Int>()
        val onError = TestEventCallback<Throwable>()
        val runtime = FormulaRuntime(
            formula = root,
            config = RuntimeConfig()
        )
        runtime.setOnOutput(onOutput)
        runtime.setOnError(onError)

        runtime.terminate()
    }

    @Test fun `it is safe to call terminate multiple times`() {
        val root = InputIdentityFormula<Int>()

        val onOutput = TestEventCallback<Int>()
        val onError = TestEventCallback<Throwable>()
        val runtime = FormulaRuntime(
            formula = root,
            config = RuntimeConfig()
        )
        runtime.setOnOutput(onOutput)
        runtime.setOnError(onError)

        runtime.onInput(0)
        runtime.terminate()
        runtime.terminate()
        runtime.terminate()
    }
}