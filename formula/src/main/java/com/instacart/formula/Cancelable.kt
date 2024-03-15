package com.instacart.formula

/**
 * Used within [Action] to receive a cancel event and perform clean up.
 */
fun interface Cancelable {
    fun cancel()
}
