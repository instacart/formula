package com.instacart.formula

/**
 * Defines an intent to transition by emitting a new [State] or [Output].
 */
data class Transition<out State, out Output> internal constructor(
    val state: State? = null,
    val effect: Output? = null
) {
    object Factory {
        fun <State, Output> output(output: Output): Transition<State, Output> {
            return Transition(null, output)
        }

        fun <State, Output> transition(state: State, output: Output? = null): Transition<State, Output> {
            return Transition(state, output)
        }
    }
}
