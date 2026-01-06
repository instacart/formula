package com.instacart.formula.benchmarks

import com.instacart.formula.Action
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot
import com.instacart.formula.test.TestFormulaObserver
import com.instacart.formula.test.test
import com.instacart.formula.test.testUnscoped
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

/**
 * Benchmark for measuring state transition performance with varying numbers of active actions.
 *
 * Tests how the number of running actions affects state change performance in a formula.
 * This is useful for understanding the overhead of action management during state updates.
 *
 * Run with:
 *   ./gradlew :formula-performance:jmh -Pjmh=ActionCountBenchmark
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@State(Scope.Thread)
open class ActionCountBenchmark {

    @Param("1", "25", "100")
    var actionCount: Int = 1

    private lateinit var observer: TestFormulaObserver<Int, ActionOutput, *>

    @Setup(Level.Iteration)
    fun setup() {
        observer = ActionFormula().testUnscoped(isValidationEnabled = false)
        observer.input(actionCount)
    }

    /**
     * Triggers 1000 state changes while N actions are active.
     * JMH will report the average time per single state change.
     */
    @Benchmark
    @OperationsPerInvocation(1000)
    fun stateChanges() {
        repeat(1000) {
            observer.output { onIncrement() }
        }
    }

    @TearDown(Level.Iteration)
    fun tearDown() {
        observer.dispose()
    }

    /**
     * Formula that runs N active actions.
     * State changes occur while all actions remain active.
     *
     * Input: number of actions to run
     */
    private class ActionFormula : Formula<Int, Int, ActionOutput>() {

        override fun initialState(input: Int) = 0

        override fun Snapshot<Int, Int>.evaluate(): Evaluation<ActionOutput> {
            return Evaluation(
                output = ActionOutput(
                    count = state,
                    actionCount = input,
                    onIncrement = context.callback {
                        transition(state + 1)
                    }
                ),
                actions = context.actions {
                    // Create N registered actions (no-op actions for overhead measurement)
                    repeat(input) { index ->
                        Action.onData(index).onEvent { event ->
                            // Actions don't update state, just remain registered
                            none()
                        }
                    }
                }
            )
        }
    }

    private data class ActionOutput(
        val count: Int,
        val actionCount: Int,
        val onIncrement: () -> Unit,
    )
}
