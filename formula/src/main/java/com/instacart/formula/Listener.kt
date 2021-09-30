package com.instacart.formula

/**
 * Formula uses Listener callbacks to pass events.
 */
fun interface Listener<Event> : (Event) -> Unit {

}

/**
 * Utility function used to specific
 */
operator fun Listener<Unit>.invoke() = invoke(Unit)