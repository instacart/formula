package com.instacart.formula

import com.google.common.truth.Truth
import com.instacart.formula.test.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class FormulaValidationTest {

    @Test
    fun `input changed during re-evaluation will throw validation error`() = runTest {
        val childFormula = object : StatelessFormula<Int, Int>() {
            override fun Snapshot<Int, Unit>.evaluate(): Int {
                return input
            }
        }

        val parentFormula = object : StatelessFormula<Unit, Int>() {
            val unstableInput = AtomicInteger(0)
            override fun Snapshot<Unit, Unit>.evaluate(): Int {
                return context.child(childFormula, unstableInput.incrementAndGet())
            }
        }

        val error = runCatching {
            parentFormula.test(coroutineContext, isValidationEnabled = true).input(Unit)
        }

        Truth.assertThat(error.exceptionOrNull()).hasMessageThat().contains(
            "- input changed during identical re-evaluation - old: 1, new: 2"
        )
    }

    @Test
    fun `output changed during re-evaluation will throw validation error`() = runTest {
        val formula = object : StatelessFormula<Unit, Int>() {
            val unstableOutput = AtomicInteger(0)
            override fun Snapshot<Unit, Unit>.evaluate(): Int {
                return unstableOutput.incrementAndGet()
            }
        }
        val error = runCatching {
            formula.test(coroutineContext, isValidationEnabled = true).input(Unit)
        }
        Truth.assertThat(error.exceptionOrNull()).hasMessageThat().contains(
            "- output changed during identical re-evaluation - old: 1, new: 2"
        )
    }

    @Test
    fun `action key changed during re-evaluation will throw validation error`() = runTest {
        val formula = object : StatelessFormula<Unit, Int>() {
            val unstableActionKey = AtomicInteger(0)
            override fun Snapshot<Unit, Unit>.evaluate(): Int {
                val action = object : Action<Unit> {
                    override fun start(
                        scope: CoroutineScope,
                        emitter: Action.Emitter<Unit>
                    ): Cancelable? {
                        return null
                    }

                    override fun key(): Any {
                        return unstableActionKey.incrementAndGet()
                    }
                }
                action.onEvent {
                    none()
                }
                return 0
            }
        }
        val error = runCatching {
            formula.test(coroutineContext, isValidationEnabled = true).input(Unit)
        }
        Truth.assertThat(error.exceptionOrNull()).hasMessageThat().contains(
            "components changed during validation - new:"
        )
    }

    @Test
    fun `action removed during re-evaluation will throw validation error`() = runTest {
        val formula = object : StatelessFormula<Unit, Int>() {
            val unstableActionKey = AtomicInteger(0)
            override fun Snapshot<Unit, Unit>.evaluate(): Int {
                changeActionKeyDuringValidation()
                return 0
            }

            private fun ActionBuilder<Unit, Unit>.changeActionKeyDuringValidation() {
                if (unstableActionKey.getAndIncrement() == 1) {
                    val action = object : Action<Unit> {
                        override fun start(
                            scope: CoroutineScope,
                            emitter: Action.Emitter<Unit>
                        ): Cancelable? {
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
            }
        }
        val error = runCatching {
            formula.test(this, isValidationEnabled = true).input(Unit)
        }
        Truth.assertThat(error.exceptionOrNull()).hasMessageThat().contains(
            "components changed during validation - new:"
        )
    }
}
