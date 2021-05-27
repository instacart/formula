package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.test.test

class DynamicStreamSubject {
    private val subject = TestFormula().test()

    fun updateStreams(vararg keys: String) = apply {
        subject.input(keys.asList())
        assertRunning(*keys)
    }

    fun removeAll() = apply {
        updateStreams()
    }

    private fun assertRunning(vararg keys: String) = apply {
        assertThat(subject.formula.running).containsExactly(*keys).inOrder()
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
