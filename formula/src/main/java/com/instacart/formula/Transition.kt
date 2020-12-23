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
        inline fun <State, T: Transition<State>> create(init: Factory.() -> T): T {
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
    object Factory {

        /**
         * A transition that does nothing.
         */
        fun none(): Transition<Nothing> {
            return None
        }

        /**
         * Creates a transition to a new [State] and executes [invokeEffects] callback
         * after the state change.
         */
        fun <State> transition(
            state: State,
            invokeEffects: (() -> Unit)? = null
        ): Stateful<State> {
            return Stateful(state, invokeEffects)
        }

        /**
         * Creates a transition that only executes [invokeEffects].
         */
        fun transition(
            invokeEffects: () -> Unit
        ): OnlyEffects {
            return OnlyEffects(invokeEffects)
        }
    }

    abstract val effects: Effects?
}
