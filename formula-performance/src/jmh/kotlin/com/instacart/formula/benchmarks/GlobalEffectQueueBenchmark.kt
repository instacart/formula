package com.instacart.formula.benchmarks

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot
import com.instacart.formula.Transition
import com.instacart.formula.test.TestFormulaObserver
import com.instacart.formula.test.test
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Benchmark for testing globalEffectQueue behavior in FormulaRuntime.
 *
 * Tests the performance of queueing and executing transition effects through the
 * globalEffectQueue. This queue accumulates effects from transitions and executes
 * them after all formulas are idle.
 *
 * The benchmark measures effect queueing and execution overhead with different
 * numbers of effects being added to the queue in a single transition.
 *
 * Run with:
 *   ./gradlew :formula-performance:jmh -Pjmh=GlobalEffectQueueBenchmark
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@State(Scope.Thread)
open class GlobalEffectQueueBenchmark {

    private lateinit var observer: TestFormulaObserver<Unit, EffectOutput, *>
    private val effectCounter = AtomicInteger(0)

    @Setup(Level.Iteration)
    fun setup() {
        observer = EffectQueueFormula(effectCounter).test(isValidationEnabled = false)
        observer.input(Unit)
        effectCounter.set(0)
    }

    /**
     * Triggers 100 transitions that each create 1 effect.
     * JMH will report the average time per single transition.
     */
    @Benchmark
    @OperationsPerInvocation(100)
    fun measure1Effect() {
        repeat(100) {
            observer.output { triggerEffects(1) }
        }
    }

    /**
     * Triggers 100 transitions that each create 10 effects.
     * JMH will report the average time per single transition.
     */
    @Benchmark
    @OperationsPerInvocation(100)
    fun measure10Effects() {
        repeat(100) {
            observer.output { triggerEffects(10) }
        }
    }

    /**
     * Triggers 100 transitions that each create 100 effects.
     * JMH will report the average time per single transition.
     */
    @Benchmark
    @OperationsPerInvocation(100)
    fun measure100Effects() {
        repeat(100) {
            observer.output { triggerEffects(100) }
        }
    }

    @TearDown(Level.Iteration)
    fun tearDown() {
        observer.dispose()
    }

    /**
     * Formula that creates multiple effects in a single transition.
     * Tests the globalEffectQueue's ability to handle many effects at once.
     *
     * Output: contains a callback that accepts the number of effects to create
     */
    private class EffectQueueFormula(
        private val effectCounter: AtomicInteger
    ) : Formula<Unit, Unit, EffectOutput>() {

        override fun initialState(input: Unit) = Unit

        override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<EffectOutput> {
            return Evaluation(
                output = EffectOutput(
                    triggerEffects = context.onEvent { effectCount: Int ->
                        // Start with no-op transition
                        var result: Transition.Result<Unit> = none()

                        // Chain effects using andThen
                        for (i in 1..effectCount) {
                            result = result.andThen {
                                transition {
                                    effectCounter.incrementAndGet()
                                }
                            }
                        }

                        result
                    }
                )
            )
        }
    }

    private data class EffectOutput(
        val triggerEffects: (Int) -> Unit,
    )
}
