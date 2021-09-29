package com.instacart.formula

/**
 * Defines an intent to transition by emitting a new [State] and optional [Effects].
 */
sealed class Transition<out State> {
    companion object {
        /**
         * A convenience method to define transitions.
         *
         * ```
         * fun nameChanged(state: FormState, newName: String) = Transition.create {
         *   transition(state.copy(name = newName))
         * }
         * ```
         */
        inline fun <State> create(init: TransitionContext.() -> Transition<State>): Transition<State> {
            return init(Factory)
        }
    }

    /**
     * Stateful transition.
     *
     * @param state New state
     * @param effects Optional effects such as parent callbacks, logging, db writes,
     * network requests, etc.
     */
    data class Stateful<State>(val state: State, override val effects: Effects? = null) : Transition<State>()

    /**
     * Only effects are emitted as part of this transition
     *
     * @param effects Effects such as parent callbacks, logging, db writes, network requests, etc.
     */
    data class OnlyEffects(override val effects: Effects) : Transition<Nothing>()

    /**
     * Nothing happens in this transition.
     */
    object None : Transition<Nothing>() {
        override val effects: Effects? = null
    }


    /**
     * Factory uses as a receiver parameter to provide transition constructor dsl.
     */
    object Factory : TransitionContext {
    }

    abstract val effects: Effects?
}