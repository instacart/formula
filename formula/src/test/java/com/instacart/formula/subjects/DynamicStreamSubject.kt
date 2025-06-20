package com.instacart.formula.subjects

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.Action
import com.instacart.formula.Cancelable
import com.instacart.formula.Evaluation
import com.instacart.formula.Snapshot
import com.instacart.formula.StatelessFormula
import com.instacart.formula.test.TestableRuntime
import kotlinx.coroutines.CoroutineScope

class DynamicStreamSubject(runtime: TestableRuntime) {
    private val subject = runtime.test(TestFormula())

    fun updateStreams(vararg keys: String) = apply {
        subject.input(keys.asList())
        assertRunning(*keys)
    }

    fun removeAll() = apply {
        updateStreams()
    }

    fun assertRunning(vararg keys: String) = apply {
        assertThat(subject.formula.running).containsExactly(*keys).inOrder()
    }

    fun dispose() = apply {
        subject.dispose()
    }

    class TestFormula : StatelessFormula<List<String>, Unit>() {
        val running = mutableListOf<String>()

        override fun Snapshot<List<String>, Unit>.evaluate(): Evaluation<Unit> {
            return Evaluation(
                output = Unit,
                actions = context.actions {
                    input.forEach { key ->
                        action(key).onEvent {
                            none()
                        }
                    }
                }
            )
        }

        private fun action(key: String): Action<Unit> {
            return object : Action<Unit> {
                override fun start(
                    scope: CoroutineScope,
                    emitter: Action.Emitter<Unit>
                ): Cancelable {
                    running.add(key)
                    return Cancelable {
                        running.remove(key)
                    }
                }

                override fun key(): Any = key
            }
        }
    }
}
