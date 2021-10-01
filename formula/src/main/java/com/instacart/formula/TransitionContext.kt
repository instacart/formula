package com.instacart.formula

/**
 * Transition context provides the current [state] and utilities to help
 * create [Transition.Result] within [Transition.toResult].
 *
 * TODO: add Formula.Input as well.
 */
interface TransitionContext<out State> {

    val state: State

    /**
     * Returns a result that indicates to do nothing as part of this event.
     */
    fun none(): Transition.Result<Nothing> {
        return Transition.Result.None
    }

    /**
     * Returns a result that contains a new [State] object and optional [effects][invokeEffects]
     * that will be executed after the state is updated.
     */
    fun <State> transition(
        state: State,
        invokeEffects: (() -> Unit)? = null
    ): Transition.Result.Stateful<State> {
        return Transition.Result.Stateful(state, invokeEffects)
    }

    /**
     * Returns a result that requests [effects][invokeEffects] to be executed.
     */
    fun transition(
        invokeEffects: () -> Unit
    ): Transition.Result.OnlyEffects {
        return Transition.Result.OnlyEffects(invokeEffects)
    }
}