package com.instacart.formula.test

import com.instacart.formula.Action
import com.instacart.formula.Cancelable
import java.lang.AssertionError

class TestActionObserver<Event>(private val action: Action<Event>) {
    private val values = mutableListOf<Event>()
    private val cancelation = action.start { values.add(it) }

    fun values(): List<Event> = values

    fun assertValues(vararg expected: Event) {
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
     * Attempts to cancel the [action]. Will throw an exception if [action] did not
     * provide a [Cancelable].
     */
    fun cancel() {
        cancelation!!.cancel()
    }
}
