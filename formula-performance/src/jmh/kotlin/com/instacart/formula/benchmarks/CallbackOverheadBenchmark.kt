package com.instacart.formula.benchmarks

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot
import com.instacart.formula.test.TestFormulaObserver
import com.instacart.formula.test.test
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

/**
 * Benchmark for measuring state transition performance with many callback declarations.
 *
 * Tests how the overhead of declaring multiple callbacks affects transition performance.
 * In real-world applications, formulas may have many callbacks for different UI interactions,
 * and this benchmark measures whether having many callbacks impacts state change performance.
 *
 * Run with:
 *   ./gradlew :formula-performance:jmh -Pjmh=CallbackOverheadBenchmark
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@State(Scope.Thread)
open class CallbackOverheadBenchmark {

    @Param("10", "50")
    var callbackCount: Int = 10

    private lateinit var observer: TestFormulaObserver<Int, CallbackOutput, *>

    @Setup(Level.Iteration)
    fun setup() {
        observer = CallbackFormula().test(isValidationEnabled = false)
    }

    /**
     * Triggers 100 transitions in succession with the specified callback count.
     * JMH will report the average time per single transition.
     */
    @Benchmark
    @OperationsPerInvocation(100)
    fun transitions() {
        observer.input(callbackCount)
        repeat(100) {
            observer.output { onIncrement() }
        }
    }

    @TearDown(Level.Iteration)
    fun tearDown() {
        observer.dispose()
    }

    /**
     * Formula that declares many callbacks to measure the overhead of callback declarations
     * on state transition performance.
     *
     * Input: number of callbacks to declare
     */
    private class CallbackFormula : Formula<Int, Int, CallbackOutput>() {

        override fun initialState(input: Int) = 0

        override fun Snapshot<Int, Int>.evaluate(): Evaluation<CallbackOutput> {
            // Create the increment callback that actually changes state
            val onIncrement = context.callback {
                transition(state + 1)
            }

            // Create a list of general callbacks that don't change state
            val generalCallbacks = List(input) { index ->
                context.callback(key = index) {
                    // Transition that returns none (no state change)
                    none()
                }
            }

            return Evaluation(
                output = CallbackOutput(
                    count = state,
                    onIncrement = onIncrement,
                    generalCallbacks = generalCallbacks
                )
            )
        }
    }

    private data class CallbackOutput(
        val count: Int,
        val onIncrement: () -> Unit,
        val generalCallbacks: List<() -> Unit>,
    )
}
