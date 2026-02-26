package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.internal.ClearPluginsRule
import com.instacart.formula.test.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class RememberRuntimeTest {

    @get:Rule
    val clearPluginsRule = ClearPluginsRule()

    @Test
    fun `remember returns same value across re-evaluations`() = runTest {
        val counter = AtomicInteger(0)
        val formula = object : StatelessFormula<Int, Int>() {
            override fun Snapshot<Int, Unit>.evaluate(): Evaluation<Int> {
                return Evaluation(
                    output = context.remember { counter.incrementAndGet() }
                )
            }
        }

        formula.test(this)
            .input(1)
            .output { assertThat(this).isEqualTo(1) }
            .input(2)
            .output { assertThat(this).isEqualTo(1) }
            .input(3)
            .output { assertThat(this).isEqualTo(1) }
            .apply {
                assertThat(counter.get()).isEqualTo(1)
            }
    }

    data class OptionalValue(val value: Int?)

    @Test
    fun `remember value is cleaned up when not requested`() = runTest {
        val counter = AtomicInteger(0)
        val formula = object : StatelessFormula<Boolean, OptionalValue>() {
            override fun Snapshot<Boolean, Unit>.evaluate(): Evaluation<OptionalValue> {
                val value = if (input) {
                    context.remember { counter.incrementAndGet() }
                } else {
                    null
                }
                return Evaluation(output = OptionalValue(value))
            }
        }

        formula.test(this)
            .input(true)
            .output { assertThat(value).isEqualTo(1) }
            .input(false)
            .output { assertThat(value).isNull() }
            .input(true)
            .output { assertThat(value).isEqualTo(2) }
    }

    @Test
    fun `multiple remember calls return distinct stable values`() = runTest {
        val counter = AtomicInteger(0)
        val formula = object : StatelessFormula<Int, List<Int>>() {
            override fun Snapshot<Int, Unit>.evaluate(): Evaluation<List<Int>> {
                val values = listOf(
                    context.remember { counter.incrementAndGet() },
                    context.remember { counter.incrementAndGet() },
                )
                return Evaluation(output = values)
            }
        }

        formula.test(this)
            .input(1)
            .output { assertThat(this).containsExactly(1, 2).inOrder() }
            .input(2)
            .output { assertThat(this).containsExactly(1, 2).inOrder() }
            .apply {
                assertThat(counter.get()).isEqualTo(2)
            }
    }

    @Test
    fun `remember with key produces new value when key changes`() = runTest {
        val counter = AtomicInteger(0)
        val formula = KeyRememberFormula(counter)

        formula.test(this)
            .input(Unit)
            .output {
                assertThat(value).isEqualTo(1)
                incrementVersion(Unit)
            }
            .output {
                assertThat(value).isEqualTo(1)
                changeKey("new")
            }
            .output {
                assertThat(value).isEqualTo(2)
                incrementVersion(Unit)
            }
            .output {
                assertThat(value).isEqualTo(2)
                changeKey("default")
            }
            .output {
                assertThat(value).isEqualTo(3)
            }
    }

    @Test
    fun `remember with shared call site disambiguated by scope`() = runTest {
        val counter = AtomicInteger(0)
        val formula = object : StatelessFormula<Int, List<Int>>() {
            override fun Snapshot<Int, Unit>.evaluate(): Evaluation<List<Int>> {
                val a = context.key("scope-a") { doRemember(context, counter) }
                val b = context.key("scope-b") { doRemember(context, counter) }
                return Evaluation(output = listOf(a, b))
            }
        }

        formula.test(this)
            .input(1)
            .output { assertThat(this).containsExactly(1, 2).inOrder() }
            .input(2)
            .output { assertThat(this).containsExactly(1, 2).inOrder() }
            .apply {
                assertThat(counter.get()).isEqualTo(2)
            }
    }

    @Test
    fun `remember in dynamic list with additions and removals`() = runTest {
        val counter = AtomicInteger(0)
        val formula = DynamicRememberFormula(counter)

        formula.test(this)
            .input(Unit)
            .output {
                assertThat(values).isEqualTo(mapOf("a" to 1, "b" to 2, "c" to 3))
                addKey("d")
            }
            .output {
                assertThat(values).isEqualTo(mapOf("a" to 1, "b" to 2, "c" to 3, "d" to 4))
                removeKey("b")
            }
            .output {
                assertThat(values).isEqualTo(mapOf("a" to 1, "c" to 3, "d" to 4))
                addKey("b")
            }
            .output {
                assertThat(values).isEqualTo(mapOf("a" to 1, "c" to 3, "d" to 4, "b" to 5))
            }
    }
}

/**
 * Non-inline function that calls [FormulaContext.remember]. Since [FormulaContext.remember] is
 * inline, all callers of this function share the same componentFactory class — they must use
 * [FormulaContext.key] to disambiguate.
 */
private fun doRemember(context: FormulaContext<*, *>, counter: AtomicInteger): Int {
    return context.remember { counter.incrementAndGet() }
}

private class KeyRememberFormula(
    private val counter: AtomicInteger,
) : Formula<Unit, KeyRememberFormula.State, KeyRememberFormula.Output>() {

    data class State(val key: String = "default", val version: Int = 0)

    data class Output(
        val value: Int,
        val changeKey: Listener<String>,
        val incrementVersion: Listener<Unit>,
    )

    override fun initialState(input: Unit) = State()

    override fun Snapshot<Unit, State>.evaluate(): Evaluation<Output> {
        return Evaluation(
            output = Output(
                value = context.remember(key = state.key) { counter.incrementAndGet() },
                changeKey = context.onEvent {
                    transition(state.copy(key = it))
                },
                incrementVersion = context.onEvent {
                    transition(state.copy(version = state.version + 1))
                },
            )
        )
    }
}

private class DynamicRememberFormula(
    private val counter: AtomicInteger,
) : Formula<Unit, DynamicRememberFormula.State, DynamicRememberFormula.Output>() {

    data class State(val keys: List<String> = listOf("a", "b", "c"))

    data class Output(
        val values: Map<String, Int>,
        val addKey: Listener<String>,
        val removeKey: Listener<String>,
    )

    override fun initialState(input: Unit) = State()

    override fun Snapshot<Unit, State>.evaluate(): Evaluation<Output> {
        val values = state.keys.associate { key ->
            key to context.key(key) { context.remember { counter.incrementAndGet() } }
        }

        return Evaluation(
            output = Output(
                values = values,
                addKey = context.onEvent {
                    transition(state.copy(keys = state.keys + it))
                },
                removeKey = context.onEvent {
                    transition(state.copy(keys = state.keys - it))
                },
            )
        )
    }
}
