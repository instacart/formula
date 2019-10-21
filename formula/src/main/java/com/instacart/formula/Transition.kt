package com.instacart.formula

/**
 * Defines an intent to transition by emitting a new [State] and optional [Effects].
 *
 * @param state Updated state
 * @param effects Optional effects such as parent callbacks / logging / db writes/ network requests / etc.
 */
data class Transition<out State> @PublishedApi internal constructor(
    val state: State? = null,
    val effects: Effects? = null
) {
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
        inline fun <State> create(init: Factory.() -> Transition<State>): Transition<State> {
            return init(Factory)
        }
    }

    object Factory {
        private val NONE = Transition<Nothing>()

        /**
         * A transition that does nothing.
         */
        fun none(): Transition<Nothing> {
            return NONE
        }

        /**
         * Creates a transition to a new [State] and executes [invokeEffects] callback
         * after the state change.
         */
        fun <State> transition(
            state: State? = null,
            invokeEffects: (() -> Unit)? = null
        ): Transition<State> {
            return Transition(state, invokeEffects)
        }

        /**
         * Creates a transition that only executes [invokeEffects].
         */
        fun transition(
            invokeEffects: () -> Unit
        ): Transition<Nothing> {
            return transition(null, invokeEffects)
        }

        /**
         * Creates a transition to a new [State] with no additional side-effects.
         */
        fun <State> State.noEffects(): Transition<State> {
            return transition(this)
        }
    }
}
