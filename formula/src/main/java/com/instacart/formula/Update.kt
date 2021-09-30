package com.instacart.formula

/**
 * An update function takes an [Event] and calculates the [Transition] that should happen. A
 * transition can contain a new [state][State] object which would trigger a [Formula.evaluate]
 * and/or [Effects] that should be performed. You can return [Transition.None] if nothing
 * should happen.
 */
fun interface Update<State, Event> {
    fun TransitionContext.create(event: Event): Transition<State>
}


