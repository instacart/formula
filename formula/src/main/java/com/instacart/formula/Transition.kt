package com.instacart.formula

/**
 * Transition is a function that is called when an [Event] happens and produces a [Result] which
 * can indicate a [state][State] change, [side-effects][Result.effects] or nothing. This result
 * will be passed and applied by the [Formula Runtime][FormulaRuntime].
 */
fun interface Transition<State, in Event> {

    /**
     * Defines a transition result which can request a state change and/or some action
     * to be performed.
     */
    sealed class Result<out State> {
        /**
         * Stateful transition.
         *
         * @param state New state
         * @param effects Optional effects such as calling listeners, logging, db writes,
         * network requests, etc.
         */
        data class Stateful<State>(val state: State, override val effects: Effects? = null) : Result<State>()

        /**
         * Only effects are emitted as part of this transition
         *
         * @param effects Effects such as calling listeners, logging, db writes, network requests, etc.
         */
        data class OnlyEffects(override val effects: Effects) : Result<Nothing>()

        /**
         * Nothing happens in this transition.
         */
        object None : Result<Nothing>() {
            override val effects: Effects? = null
        }

        /**
         * Optional side-effects function that will be executed by [Formula Runtime][FormulaRuntime].
         */
        abstract val effects: Effects?
    }

    /**
     * Called when an [Event] happens and creates a [Result] type which can indicate a state
     * change and/or some executable effects. Use [TransitionContext.none] if nothing should happen
     * as part of this event.
     */
    fun TransitionContext<State>.toResult(event: Event): Result<State>
}