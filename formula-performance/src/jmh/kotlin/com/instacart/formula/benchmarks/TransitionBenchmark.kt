package com.instacart.formula.benchmarks

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.IFormula
import com.instacart.formula.Snapshot
import com.instacart.formula.test.TestFormulaObserver
import com.instacart.formula.test.test
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

/**
 * Benchmark for formula transition performance at various nesting depths.
 *
 * Measures how efficiently Formula handles state transitions through
 * nested child formulas. Tests both the fundamental transition performance
 * (depth=0) and the overhead of context.child() calls at deeper nesting levels.
 *
 * Run with:
 *   ./gradlew :formula-performance:jmh -Pjmh=TransitionBenchmark
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@State(Scope.Thread)
open class TransitionBenchmark {

    @Param("0", "1", "2", "5", "10", "25")
    var depth: Int = 0

    private lateinit var observer: TestFormulaObserver<Unit, TransitionOutput, *>

    @Setup(Level.Trial)
    fun setup() {
        observer = initFormula(depth).test().apply { input(Unit) }
    }

    /**
     * Triggers 100 transitions in succession.
     * JMH will report the average time per single transition.
     */
    @Benchmark
    @OperationsPerInvocation(100)
    fun transitions() {
        repeat(100) {
            observer.output { onIncrement() }
        }
    }

    @TearDown(Level.Trial)
    fun tearDown() {
        observer.dispose()
    }

    private fun initFormula(depth: Int): IFormula<Unit, TransitionOutput> {
        return if (depth == 0) {
            LeafFormula()
        } else {
            NestedTransitionFormula(
                childFormula = initFormula(depth - 1)
            )
        }
    }

    /**
     * Formula that nests children to a specified depth.
     * State changes occur at the deepest child level.
     *
     * The nested hierarchy is created recursively at construction time.
     */
    private class NestedTransitionFormula(
        private val childFormula: IFormula<Unit, TransitionOutput>,
    ) : Formula<Unit, Unit, TransitionOutput>() {

        override fun initialState(input: Unit) = Unit

        override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<TransitionOutput> {
            val output = context.child(childFormula)
            return Evaluation(output)
        }
    }

    /**
     * Leaf formula at the deepest level with actual state.
     */
    private class LeafFormula : Formula<Unit, Int, TransitionOutput>() {
        override fun initialState(input: Unit) = 0

        override fun Snapshot<Unit, Int>.evaluate(): Evaluation<TransitionOutput> {
            return Evaluation(
                output = TransitionOutput(
                    count = state,
                    onIncrement = context.callback {
                        transition(state + 1)
                    }
                )
            )
        }
    }

    private data class TransitionOutput(
        val count: Int,
        val onIncrement: () -> Unit,
    )
}
