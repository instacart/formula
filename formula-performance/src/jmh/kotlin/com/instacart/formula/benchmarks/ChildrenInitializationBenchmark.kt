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
 * Benchmark for measuring child formula initialization performance.
 *
 * Tests the cost of creating brand new child formulas (not reusing existing ones).
 * Each iteration creates children with different keys, forcing initialState() calls.
 * Measures combined cost of removing old children and initializing new ones.
 *
 * Run with:
 *   ./gradlew :formula-performance:jmh -Pjmh=ChildrenInitializationBenchmark
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@State(Scope.Thread)
open class ChildrenInitializationBenchmark {

    @Param("1", "25", "100")
    var childrenCount: Int = 1

    private lateinit var observer: TestFormulaObserver<InitInput, ParentOutput, *>
    private var generation = 0

    @Setup(Level.Iteration)
    fun setup() {
        observer = ParentFormula().testUnscoped(isValidationEnabled = false)
        generation = 0
        // Warmup: trigger initial formula setup outside measurement
        observer.input(InitInput(childrenCount, generation++))
    }

    /**
     * Measures child removal (old generation) + initialization (new generation).
     * Initial formula setup is excluded via warmup in setup().
     *
     * Triggers 100 evaluations where each creates brand new children.
     * By changing the generation, all child keys change, forcing initialization.
     */
    @Benchmark
    @OperationsPerInvocation(100)
    fun initializeNewChildren() {
        repeat(100) {
            observer.input(InitInput(childrenCount, generation++))
        }
    }

    @TearDown(Level.Iteration)
    fun tearDown() {
        observer.dispose()
    }

    private class ParentFormula : Formula<InitInput, Unit, ParentOutput>() {

        override fun initialState(input: InitInput) = Unit

        override fun Snapshot<InitInput, Unit>.evaluate(): Evaluation<ParentOutput> {
            // Each child gets a unique input/key based on generation
            // When generation changes, all children are NEW (not reused)
            val childOutputs = List(input.count) { index ->
                context.child(
                    formula = ChildFormula(),
                    input = ChildInput(
                        id = index,
                        generation = input.generation
                    )
                )
            }

            return Evaluation(
                output = ParentOutput(
                    childCount = childOutputs.size,
                    generation = input.generation
                )
            )
        }
    }

    /**
     * Simple child formula with minimal state.
     * Uses generation in key to force re-initialization when generation changes.
     */
    private class ChildFormula : Formula<ChildInput, Unit, ChildOutput>() {

        override fun initialState(input: ChildInput) = Unit

        override fun key(input: ChildInput): Any = Pair(input.id, input.generation)

        override fun Snapshot<ChildInput, Unit>.evaluate(): Evaluation<ChildOutput> {
            return Evaluation(
                output = ChildOutput(
                    id = input.id,
                    generation = input.generation
                )
            )
        }
    }

    private data class InitInput(
        val count: Int,
        val generation: Int
    )

    private data class ChildInput(
        val id: Int,
        val generation: Int
    )

    private data class ChildOutput(
        val id: Int,
        val generation: Int
    )

    private data class ParentOutput(
        val childCount: Int,
        val generation: Int
    )
}
