package com.instacart.formula

/**
 *
 * TODO: document / find a better name.
 *
 * Maybe `Dispatcher`?
 *
 * Naming?
 * - State
 * - Update
 * - Transition
 * - Action
 * - Event
 * - Action -> Event -> Transition/Update ->
 *
 * What am I doing here?
 * - I want to clearly capture the meaning of event handling.
 *
 * Issues:
 * - Typealiases are not good for inheritance.
 */
fun interface UpdateType<State, Event> {
    fun TransitionContext.create(event: Event): Transition<State>
}

/*
 * UpdateName
 */
class UpdateNameV2 : UpdateType<String, String> {
    override fun TransitionContext.create(event: String): Transition<String> {
        return transition(state = event)
    }
}


