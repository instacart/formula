package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.test.test
import com.jakewharton.rxrelay2.PublishRelay

class DynamicStreamSubject {
    private val streams = PublishRelay.create<List<String>>()
    private val subject = TestFormula().test(streams)

    fun updateStreams(vararg keys: String) = apply {
        streams.accept(keys.asList())
        assertRunning(*keys)
    }

    fun removeAll() = apply {
        updateStreams()
    }

    private fun assertRunning(vararg keys: String) = apply {
        assertThat(subject.formula.running).containsExactly(*keys)
    }

    class TestFormula : StatelessFormula<List<String>, Unit>() {
        val running = mutableListOf<String>()

        override fun evaluate(input: List<String>, context: FormulaContext<Unit>): Evaluation<Unit> {
            return Evaluation(
                renderModel = Unit,
                updates = context.updates {
                    input.forEach { key ->
                        events(key, stream(key)) {
                            none()
                        }
                    }
                }
            )
        }

        private fun stream(key: String): Stream<Unit, Unit> {
            return object : Stream<Unit, Unit> {
                override fun start(input: Unit, onEvent: (Unit) -> Unit): Cancelation? {
                    running.add(key)
                    return Cancelation {
                        running.remove(key)
                    }
                }
            }
        }
    }
}
