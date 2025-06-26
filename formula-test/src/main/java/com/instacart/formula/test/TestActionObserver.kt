package com.instacart.formula.test

import com.instacart.formula.Action
import com.instacart.formula.Cancelable
import kotlinx.coroutines.CoroutineScope
import java.lang.AssertionError

class TestActionObserver<Event>(
    private val action: Action<Event>,
    private val scope: CoroutineScope,
) {
    private val values = mutableListOf<Event>()
    private val errors = mutableListOf<Throwable>()
    private val cancelable = action.start(
        scope = scope,
        emitter = object : Action.Emitter<Event> {
            override fun onEvent(event: Event) {
                values.add(event)
            }

            override fun onError(throwable: Throwable) {
                errors.add(throwable)
            }
        }
    )

    init {
        assertNoErrors()
    }

    fun values(): List<Event> = values

    fun assertValues(vararg expected: Event) {
        assertNoErrors()

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
        assertNoErrors()

        val cancelable = cancelable ?: run {
            throw IllegalStateException("Action did not return a cancelable.")
        }

        cancelable.cancel()

        assertNoErrors()
    }

    private fun assertNoErrors() {
        if (errors.isNotEmpty()) {
            throw AssertionError("Expected no errors, but got: $errors")
        }
    }
}
