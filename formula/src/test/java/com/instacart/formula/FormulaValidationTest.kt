package com.instacart.formula

import com.google.common.truth.Truth
import com.instacart.formula.test.test
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class FormulaValidationTest {

    @Test
    fun `input changed during re-evaluation will throw validation error`() {
        val childFormula = object : StatelessFormula<Int, Int>() {
            override fun Snapshot<Int, Unit>.evaluate(): Evaluation<Int> {
                return Evaluation(output = input)
            }
        }

        val parentFormula = object : StatelessFormula<Unit, Int>() {
            val unstableInput = AtomicInteger(0)
            override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Int> {
                return Evaluation(
                    output = context.child(childFormula, unstableInput.incrementAndGet())
                )
            }
        }

        val error = runCatching {
            parentFormula.test(isValidationEnabled = true).input(Unit)
        }

        Truth.assertThat(error.exceptionOrNull()).hasMessageThat().contains(
            "- input changed during identical re-evaluation - old: 1, new: 2"
        )
    }

    @Test
    fun `output changed during re-evaluation will throw validation error`() {
        val formula = object : StatelessFormula<Unit, Int>() {
            val unstableOutput = AtomicInteger(0)
            override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Int> {
                return Evaluation(
                    output = unstableOutput.incrementAndGet()
                )
            }
        }
        val error = runCatching {
            formula.test(isValidationEnabled = true).input(Unit)
        }
        Truth.assertThat(error.exceptionOrNull()).hasMessageThat().contains(
            "- output changed during identical re-evaluation - old: 1, new: 2"
        )
    }

    @Test
    fun `action key changed during re-evaluation will throw validation error`() {
        val formula = object : StatelessFormula<Unit, Int>() {
            val unstableActionKey = AtomicInteger(0)
            override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Int> {
                return Evaluation(
                    output = 0,
                    context.actions {
                        val action = object : Action<Unit> {
                            override fun start(send: (Unit) -> Unit): Cancelable? {
                                return null
                            }

                            override fun key(): Any {
                                return unstableActionKey.incrementAndGet()
                            }
                        }

                        action.onEvent {
                            none()
                        }
                    }
                )
            }
        }
        val error = runCatching {
            formula.test(isValidationEnabled = true).input(Unit)
        }
        Truth.assertThat(error.exceptionOrNull()).hasMessageThat().contains(
            "action keys changed during identical re-evaluation"
        )
    }
}