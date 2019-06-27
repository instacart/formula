package com.instacart.formula

/**
 * Converts an impure side-effect into a data type. Used to ensure
 * predictable execution of the state machine.
 */
data class SideEffect(val key: String, val effect: () -> Unit)
