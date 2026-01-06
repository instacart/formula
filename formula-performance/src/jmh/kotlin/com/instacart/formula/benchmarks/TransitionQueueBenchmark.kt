package com.instacart.formula.benchmarks

import com.instacart.formula.Action
import com.instacart.formula.Cancelable
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot
import com.instacart.formula.test.TestFormulaObserver
import com.instacart.formula.test.test
import com.instacart.formula.test.testUnscoped
import kotlinx.coroutines.CoroutineScope
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

/**
 * Benchmark for testing transitionQueue behavior in FormulaManagerImpl.
 *
 * Tests the performance of inline state transitions when an action emits
 * multiple events on start. This exercises the transitionQueue mechanism
 * which queues up transitions while isRunning=true and processes them inline.
 *
 * Run with:
 *   ./gradlew :formula-performance:jmh -Pjmh=TransitionQueueBenchmark
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@State(Scope.Thread)
open class TransitionQueueBenchmark {

    private lateinit var observer: TestFormulaObserver<Int, Int, *>

    @Setup(Level.Iteration)
    fun setup() {
        observer = MultiEventFormula().testUnscoped(isValidationEnabled = false)
        observer.input(0)
    }

    @Benchmark
    fun measure1() {
        observer.input(1)
    }

    @Benchmark
    fun measure100() {
        observer.input(100)
    }

    @Benchmark
    fun measure10000() {
        observer.input(10000)
    }

    @TearDown(Level.Iteration)
    fun tearDown() {
        observer.dispose()
    }

    /**
     * Formula that uses an action which emits multiple events on start.
     * This tests the transitionQueue behavior where events are queued and processed inline.
     *
     * Input: number of events to emit (0 means no action)
     * Output: counter value from last event
     */
    private class MultiEventFormula : Formula<Int, Int, Int>() {

        override fun initialState(input: Int) = 0

        override fun Snapshot<Int, Int>.evaluate(): Evaluation<Int> {
            return Evaluation(
                output = state,
                actions = context.actions {
                    if (input > 0) {
                        // Action that emits multiple events on start
                        MultiEmitAction(input).onEvent { event ->
                            transition(event)
                        }
                    }
                }
            )
        }
    }

    /**
     * Custom action that emits multiple events synchronously on start.
     * All events are emitted immediately, causing them to queue up in transitionQueue.
     */
    private class MultiEmitAction(private val eventCount: Int) : Action<Int> {
        override fun start(scope: CoroutineScope, emitter: Action.Emitter<Int>): Cancelable? {
            // Emit multiple events synchronously - these will queue up in transitionQueue
            for (i in 1..eventCount) {
                emitter.onEvent(i)
            }
            return null
        }

        override fun key(): Any = eventCount
    }
}
