package com.instacart.formula

interface TransitionContext {
    /**
     * A transition that does nothing.
     */
    fun none(): Transition<Nothing> {
        return Transition.None
    }

    /**
     * Creates a transition to a new [State] and executes [invokeEffects] callback
     * after the state change.
     */
    fun <State> transition(
        state: State,
        invokeEffects: (() -> Unit)? = null
    ): Transition.Stateful<State> {
        return Transition.Stateful(state, invokeEffects)
    }

    /**
     * Creates a transition that only executes [invokeEffects].
     */
    fun transition(
        invokeEffects: () -> Unit
    ): Transition.OnlyEffects {
        return Transition.OnlyEffects(invokeEffects)
    }
}