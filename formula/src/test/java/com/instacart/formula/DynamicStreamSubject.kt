package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.test.test
import com.instacart.formula.utils.TestUtils
import com.jakewharton.rxrelay2.PublishRelay

class DynamicStreamSubject {
    private val streams = PublishRelay.create<List<String>>()
    private val running = mutableListOf<String>()
    private val streamFactory = { key: String -> stream(running, key) }
    private val subject = formula(streamFactory).test(streams)

    fun updateStreams(vararg keys: String) = apply {
        streams.accept(keys.asList())
        assertRunning(*keys)
    }

    fun removeAll() = apply {
        updateStreams()
    }

    private fun assertRunning(vararg keys: String) = apply {
        assertThat(running).containsExactly(*keys)
    }

    private fun formula(stream: (key: String) -> Stream<Unit>) = TestUtils.stateless { input: List<String>, context ->
        Evaluation(
            renderModel = Unit,
            updates = context.updates {
                input.forEach { key ->
                    events(stream(key)) {
                        none()
                    }
                }
            }
        )
    }

    private fun stream(running: MutableList<String>, key: String): Stream<Unit> {
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
