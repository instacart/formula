package com.instacart.formula

import app.cash.turbine.test
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.internal.TestPlugin
import com.instacart.formula.internal.TestDispatcher
import com.instacart.formula.plugin.FormulaError
import com.instacart.formula.plugin.withPlugin
import com.instacart.formula.test.test
import com.instacart.formula.types.IncrementFormula
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class StateFlowRuntimeTest {

    @Test fun `formula with no input`() = runTest {
        val formula = object : StatelessFormula<Unit, Int>() {
            override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Int> {
                return Evaluation(
                    output = 1,
                )
            }
        }

        val stateFlow = formula.runAsStateFlow(backgroundScope)
        stateFlow.test {
            assertThat(awaitItem()).isEqualTo(1)
        }
    }

    @Test fun `formula with input`() = runTest {
        val formula = object : StatelessFormula<Int, Int>() {
            override fun Snapshot<Int, Unit>.evaluate(): Evaluation<Int> {
                return Evaluation(
                    output = input,
                )
            }
        }

        val stateFlow = formula.runAsStateFlow(backgroundScope, 1)
        stateFlow.test {
            assertThat(awaitItem()).isEqualTo(1)
        }
    }

    @Test fun `state updates are propagated`() = runTest {
        val eventBus = MutableSharedFlow<Int>(
            extraBufferCapacity = Int.MAX_VALUE,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )

        val formula = object : Formula<Unit, Int, Int>() {
            override fun initialState(input: Unit): Int = 0

            override fun Snapshot<Unit, Int>.evaluate(): Evaluation<Int> {
                return Evaluation(
                    output = state,
                    actions = context.actions {
                        Action.fromFlow { eventBus }.onEvent {
                            transition(it)
                        }
                    }
                )
            }
        }

        val stateFlow = formula.runAsStateFlow(backgroundScope)
        stateFlow.test {
            assertThat(awaitItem()).isEqualTo(0)

            eventBus.emit(1)
            assertThat(awaitItem()).isEqualTo(1)

            eventBus.emit(2)
            assertThat(awaitItem()).isEqualTo(2)
        }
    }

    @Test fun `once parent scope is cancelled, state flow does not emit any new events`() = runTest {
        val formula = IncrementFormula()

        val scope = CoroutineScope(Job())
        val stateFlow = formula.runAsStateFlow(scope)
        stateFlow.test {
            assertThat(awaitItem().value).isEqualTo(0)

            stateFlow.value.onIncrement()
            assertThat(awaitItem().value).isEqualTo(1)

            scope.cancel()

            stateFlow.value.onIncrement()
            expectNoEvents()

            val remainingItems = cancelAndConsumeRemainingEvents()
            assertThat(remainingItems).isEmpty()
        }
    }

    @Test fun `error in initial evaluation throws an exception`() = runTest {
        val formula = object : StatelessFormula<Unit, Int>() {
            override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Int> {
                throw IllegalStateException("Error in evaluation")
            }
        }

        val result = runCatching { formula.runAsStateFlow(backgroundScope) }

        val exception = result.exceptionOrNull()
        assertThat(exception?.message).isEqualTo("Output is not available yet")
    }

    @Test fun `throws an exception if coroutine scope does not contain a job`() = runTest {
        val formula = object : StatelessFormula<Unit, Int>() {
            override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Int> {
                return Evaluation(
                    output = 1,
                )
            }
        }

        val scope = object : CoroutineScope {
            override val coroutineContext: CoroutineContext = EmptyCoroutineContext
        }
        val result = runCatching { formula.runAsStateFlow(scope) }
        val exception = result.exceptionOrNull()
        assertThat(exception).isInstanceOf(IllegalStateException::class.java)
        assertThat(exception?.message).isEqualTo("Current context doesn't contain Job in it: EmptyCoroutineContext")
    }

    @Test fun `default dispatcher is not used within runAsStateFlow`() {
        val blockingDispatcher = TestDispatcher()
        withPlugin(TestPlugin(defaultDispatcher = blockingDispatcher)) {
            runTest {
                val formula = object : StatelessFormula<Unit, Int>() {
                    override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Int> {
                        return Evaluation(
                            output = 1,
                        )
                    }
                }

                val stateFlow = formula.runAsStateFlow(backgroundScope)
                stateFlow.test {
                    assertThat(awaitItem()).isEqualTo(1)
                }
            }
        }
    }

    @Test fun `formula runAsStateFlow errors is propagated to global error handler`() = runTest {
        val error = IllegalStateException("something went wrong")
        val formula = object : StatelessFormula<Unit, Unit>() {
            override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Unit> {
                throw error
            }
        }

        val plugin = TestPlugin()
        withPlugin(plugin) {
            runCatching { formula.runAsStateFlow(backgroundScope) }

            assertThat(plugin.errors).containsExactly(
                FormulaError.Unhandled(formula.type(), error)
            )
        }
    }
}