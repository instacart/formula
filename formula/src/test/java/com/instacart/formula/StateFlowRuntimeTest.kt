package com.instacart.formula

import app.cash.turbine.test
import com.google.common.truth.Truth
import com.instacart.formula.internal.TestDispatcher
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
            Truth.assertThat(awaitItem()).isEqualTo(1)
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
            Truth.assertThat(awaitItem()).isEqualTo(1)
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
            Truth.assertThat(awaitItem()).isEqualTo(0)

            eventBus.emit(1)
            Truth.assertThat(awaitItem()).isEqualTo(1)

            eventBus.emit(2)
            Truth.assertThat(awaitItem()).isEqualTo(2)
        }
    }

    @Test fun `once parent scope is cancelled, state flow does not emit any new events`() = runTest {
        val formula = IncrementFormula()

        val scope = CoroutineScope(Job())
        val stateFlow = formula.runAsStateFlow(scope)
        stateFlow.test {
            Truth.assertThat(awaitItem().value).isEqualTo(0)

            stateFlow.value.onIncrement()
            Truth.assertThat(awaitItem().value).isEqualTo(1)

            scope.cancel()

            stateFlow.value.onIncrement()
            expectNoEvents()

            val remainingItems = cancelAndConsumeRemainingEvents()
            Truth.assertThat(remainingItems).isEmpty()
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
        Truth.assertThat(exception?.message).isEqualTo("Output is not available yet")
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
        Truth.assertThat(exception).isInstanceOf(IllegalStateException::class.java)
        Truth.assertThat(exception?.message).isEqualTo("Current context doesn't contain Job in it: EmptyCoroutineContext")
    }

    @Test fun `background dispatcher should not be used for initial evaluation`() = runTest {
        val formula = object : StatelessFormula<Unit, Int>() {
            override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Int> {
                return Evaluation(
                    output = 1,
                )
            }
        }

        val blockingDispatcher = TestDispatcher()
        val config = RuntimeConfig(defaultDispatcher = blockingDispatcher)
        val stateFlow = formula.runAsStateFlow(backgroundScope, config)
        stateFlow.test {
            Truth.assertThat(awaitItem()).isEqualTo(1)
        }
    }
}