package com.instacart.formula.internal

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.Stream
import com.instacart.formula.Update
import io.reactivex.disposables.Disposable
import org.junit.Before
import org.junit.Test

class UpdateManagerTest {
    private lateinit var transitionLock: TestTransitionLock
    private lateinit var manager: UpdateManager

    lateinit var running: MutableList<String>

    @Before fun setup() {
        transitionLock = TestTransitionLock()
        manager = UpdateManager(transitionLock)
        running = mutableListOf()
    }

    @Test fun `removes all`() {
        val initial = listOf(
            createStream("one"),
            createStream("two"),
            createStream("three")
        )

        manager.updateConnections(initial, 0)
        assertThat(running).containsExactly("one", "two", "three")

        manager.updateConnections(emptyList(), 0)

        assertThat(running).isEmpty()
    }

    @Test fun `switch one`() {
        val initial = listOf(
            createStream("one"),
            createStream("two"),
            createStream("three")
        )

        manager.updateConnections(initial, 0)

        val next = listOf(
            createStream("one"),
            createStream("three"),
            createStream("four")
        )

        manager.updateConnections(next, 0)

        assertThat(running).containsExactly("one", "three", "four")
    }

    private fun createStream(key: String): Update.Stream<*, *> {
        return Update.Stream(
            key = Update.Stream.Key(
                Unit,
                Stream::class,
                tag = key
            ),
            stream = object : Stream<Unit, Unit> {
                override fun subscribe(input: Unit, onEvent: (Unit) -> Unit): Disposable {
                    running.add(key)
                    return object : Disposable {
                        override fun isDisposed(): Boolean {
                            return !running.contains(key)
                        }

                        override fun dispose() {
                            running.remove(key)
                        }
                    }
                }
            },
            input = Unit,
            onEvent = {}
        )
    }

    class TestTransitionLock : TransitionLock {
        var hasTransitioned = false

        override fun hasTransitioned(transitionNumber: Long): Boolean {
            return hasTransitioned
        }
    }
}
