package com.instacart.formula.test

import com.instacart.formula.Action
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

class TestCoroutineSchedulerIntegrationTest {

    private class DelayFormula : Formula<Unit, DelayFormula.State, Int>() {
        data class State(val count: Int = 0)

        override fun initialState(input: Unit) = State()

        override fun Snapshot<Unit, State>.evaluate(): Evaluation<Int> {
            return Evaluation(
                output = state.count,
                actions = context.actions {
                    Action.fromFlow {
                        flow {
                            delay(1.seconds)
                            emit(1)
                        }
                    }.onEvent {
                        transition(state.copy(count = state.count + 1))
                    }
                }
            )
        }
    }

    @Test
    fun `TestCoroutineScheduler controls delay timing in formula actions`() = runTest {
        val scheduler = TestCoroutineScheduler()
        val formula = DelayFormula()
        val observer = formula.test(coroutineScheduler = scheduler)

        observer.input(Unit)
        observer.output { assert(this == 0) }

        // Advance virtual time by 2 seconds (longer than the 1-second delay)
        scheduler.advanceTimeBy(2.seconds)

        // The action should have completed, incrementing count to 1
        observer.output { assert(this == 1) }

        observer.dispose()
    }
}