package com.instacart.formula.benchmarks

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot
import com.instacart.formula.test.TestFormulaObserver
import com.instacart.formula.test.test
import com.instacart.formula.test.testUnscoped
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

/**
 * Benchmark for measuring state transition performance with varying numbers of child formulas.
 *
 * Tests how the number of active child formulas affects parent state change performance.
 * This is useful for understanding the cost of composing many child formulas in a parent.
 *
 * Run with:
 *   ./gradlew :formula-performance:jmh -Pjmh=ChildrenCountBenchmark
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@State(Scope.Thread)
open class ChildrenCountBenchmark {

    @Param("1", "25", "100")
    var childrenCount: Int = 1

    private lateinit var observer: TestFormulaObserver<Int, ParentOutput, *>

    @Setup(Level.Iteration)
    fun setup() {
        observer = ParentFormula().testUnscoped(isValidationEnabled = false)
        observer.input(childrenCount)
    }

    /**
     * Triggers 1000 transitions in the parent formula.
     * JMH will report the average time per single transition.
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
     * Parent formula that manages N child formulas.
     * State changes occur in the parent while children remain stable.
     *
     * Input: number of children to create
     */
    private class ParentFormula : Formula<Int, Int, ParentOutput>() {

        override fun initialState(input: Int) = 0

        override fun Snapshot<Int, Int>.evaluate(): Evaluation<ParentOutput> {
            // Create N children
            val childOutputs = List(input) { index ->
                context.child(
                    formula = ChildFormula(),
                    input = ChildInput(id = index)
                )
            }

            return Evaluation(
                output = ParentOutput(
                    count = state,
                    childCount = childOutputs.size,
                    onIncrement = context.callback {
                        transition(state + 1)
                    }
                )
            )
        }
    }

    /**
     * Simple child formula with minimal state.
     */
    private class ChildFormula : Formula<ChildInput, Unit, ChildOutput>() {

        override fun initialState(input: ChildInput) = Unit

        override fun key(input: ChildInput): Any = input.id

        override fun Snapshot<ChildInput, Unit>.evaluate(): Evaluation<ChildOutput> {
            return Evaluation(
                output = ChildOutput(id = input.id)
            )
        }
    }

    private data class ChildInput(val id: Int)
    private data class ChildOutput(val id: Int)

    private data class ParentOutput(
        val count: Int,
        val childCount: Int,
        val onIncrement: () -> Unit,
    )
}
