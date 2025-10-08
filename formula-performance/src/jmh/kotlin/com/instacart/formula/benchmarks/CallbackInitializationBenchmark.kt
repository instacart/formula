package com.instacart.formula.benchmarks

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot
import com.instacart.formula.test.TestFormulaObserver
import com.instacart.formula.test.test
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

/**
 * Benchmark for measuring callback initialization performance.
 *
 * Tests the cost of creating brand new callbacks (not redeclaring existing ones).
 * Each iteration creates callbacks with different keys, forcing initialization from scratch.
 * Measures combined cost of removing old callbacks and initializing new ones.
 *
 * Run with:
 *   ./gradlew :formula-performance:jmh -Pjmh=CallbackInitializationBenchmark
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@State(Scope.Thread)
open class CallbackInitializationBenchmark {

    @Param("10", "50")
    var callbackCount: Int = 10

    private lateinit var observer: TestFormulaObserver<InitInput, CallbackOutput, *>
    private var generation = 0

    @Setup(Level.Iteration)
    fun setup() {
        observer = CallbackInitFormula().test(isValidationEnabled = false)
        generation = 0
        // Warmup: trigger initial formula setup outside measurement
        observer.input(InitInput(callbackCount, generation++))
    }

    /**
     * Measures callback removal (old generation) + initialization (new generation).
     * Initial formula setup is excluded via warmup in setup().
     *
     * Triggers 100 evaluations where each creates brand new callbacks.
     * By changing the generation, all callback keys change, forcing initialization.
     */
    @Benchmark
    @OperationsPerInvocation(100)
    fun initializeNewCallbacks() {
        repeat(100) {
            observer.input(InitInput(callbackCount, generation++))
        }
    }

    @TearDown(Level.Iteration)
    fun tearDown() {
        observer.dispose()
    }

    private class CallbackInitFormula : Formula<InitInput, Unit, CallbackOutput>() {

        override fun initialState(input: InitInput) = Unit

        override fun Snapshot<InitInput, Unit>.evaluate(): Evaluation<CallbackOutput> {
            // Each callback gets a unique key based on generation
            // When generation changes, all callbacks are NEW (not redeclared)
            val callbacks = List(input.count) { index ->
                context.callback(key = Pair(index, input.generation)) {
                    none()
                }
            }

            return Evaluation(
                output = CallbackOutput(
                    callbacks = callbacks,
                    generation = input.generation
                )
            )
        }
    }

    private data class InitInput(
        val count: Int,
        val generation: Int
    )

    private data class CallbackOutput(
        val callbacks: List<() -> Unit>,
        val generation: Int
    )
}
