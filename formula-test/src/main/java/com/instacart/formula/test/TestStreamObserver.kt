package com.instacart.formula.test

import com.instacart.formula.Cancelable
import com.instacart.formula.Stream
import java.lang.AssertionError

class TestStreamObserver<Message>(private val stream: Stream<Message>) {
    private val values = mutableListOf<Message>()
    private val cancelation = stream.start { values.add(it) }

    fun values(): List<Message> = values

    fun assertValues(vararg expected: Message) {
        if (expected.size != values.size) {
            throw AssertionError("Value count differs; expected: ${expected.size}, was: ${values.size}")
        }

        expected.zip(values).forEachIndexed { index, (expected, value) ->
            if (expected != value) {
                throw AssertionError("Values are different at $index; expected: $expected, was: $value")
            }
        }
    }

    /**
     * Attempts to cancel the [stream]. Will throw an exception if [stream] did not
     * provide a [Cancelable].
     */
    fun cancel() {
        cancelation!!.cancel()
    }
}
