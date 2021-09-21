package com.instacart.formula.subjects

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.Cancelable
import com.instacart.formula.Evaluation
import com.instacart.formula.FormulaContext
import com.instacart.formula.StatelessFormula
import com.instacart.formula.Stream
import com.instacart.formula.test.TestableRuntime

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

        override fun evaluate(input: List<String>, context: FormulaContext<Unit>): Evaluation<Unit> {
            return Evaluation(
                output = Unit,
                updates = context.updates {
                    input.forEach { key ->
                        events(stream(key)) {
                            none()
                        }
                    }
                }
            )
        }

        private fun stream(key: String): Stream<Unit> {
            return object : Stream<Unit> {
                override fun start(send: (Unit) -> Unit): Cancelable? {
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
