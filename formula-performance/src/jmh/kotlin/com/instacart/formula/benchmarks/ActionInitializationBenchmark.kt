package com.instacart.formula.benchmarks

import com.instacart.formula.Action
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot
import com.instacart.formula.test.TestFormulaObserver
import com.instacart.formula.test.test
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

/**
 * Benchmark for measuring action initialization performance.
 *
 * Tests the cost of creating brand new actions (not redeclaring existing ones).
 * Each iteration creates actions with different keys, forcing initialization from scratch.
 * Measures combined cost of cancelling old actions and starting new ones.
 *
 * Run with:
 *   ./gradlew :formula-performance:jmh -Pjmh=ActionInitializationBenchmark
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@State(Scope.Thread)
open class ActionInitializationBenchmark {

    @Param("1", "25", "100")
    var actionCount: Int = 1

    private lateinit var observer: TestFormulaObserver<InitInput, ActionOutput, *>
    private var generation = 0

    @Setup(Level.Iteration)
    fun setup() {
        observer = ActionInitFormula().test(isValidationEnabled = false)
        generation = 0
        // Warmup: trigger initial formula setup outside measurement
        observer.input(InitInput(actionCount, generation++))
    }

    /**
     * Measures action cancellation (old generation) + initialization (new generation).
     * Initial formula setup is excluded via warmup in setup().
     *
     * Triggers 100 evaluations where each creates brand new actions.
     * By changing the generation, all action keys change, forcing initialization.
     */
    @Benchmark
    @OperationsPerInvocation(100)
    fun initializeNewActions() {
        repeat(100) {
            observer.input(InitInput(actionCount, generation++))
        }
    }

    @TearDown(Level.Iteration)
    fun tearDown() {
        observer.dispose()
    }

    private class ActionInitFormula : Formula<InitInput, Unit, ActionOutput>() {

        override fun initialState(input: InitInput) = Unit

        override fun Snapshot<InitInput, Unit>.evaluate(): Evaluation<ActionOutput> {
            return Evaluation(
                output = ActionOutput(
                    actionCount = input.count,
                    generation = input.generation
                ),
                actions = context.actions {
                    // Each action gets a unique key based on generation
                    // When generation changes, all actions are NEW (not redeclared)
                    repeat(input.count) { index ->
                        Action.onData(Pair(index, input.generation)).onEvent { event ->
                            none()
                        }
                    }
                }
            )
        }
    }

    private data class InitInput(
        val count: Int,
        val generation: Int
    )

    private data class ActionOutput(
        val actionCount: Int,
        val generation: Int
    )
}
