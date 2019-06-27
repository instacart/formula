package com.instacart.formula

/**
 * Defines an intent to transition by emitting a new [State] or [Output].
 *
 * @param state Updated state
 * @param output An optional message to the parent [Formula].
 * @param sideEffects Optional side-effects such as logging / db writes / network requests / etc.
 */
data class Transition<out State, out Output> internal constructor(
    val state: State? = null,
    val output: Output? = null,
    val sideEffects: List<SideEffect> = emptyList()
) {
    object Factory {
        /**
         * A transition that does nothing.
         */
        fun <State, Output> none(): Transition<State, Output> {
            return Transition()
        }

        /**
         * Emits an [Output] event to the parent [Formula].
         */
        fun <State, Output> output(output: Output): Transition<State, Output> {
            return Transition(null, output)
        }

        /**
         * Triggers a transition to new [State] that also can have an optional [Output].
         */
        fun <State, Output> transition(
            state: State,
            output: Output? = null,
            sideEffects: List<SideEffect> = emptyList()
        ): Transition<State, Output> {
            return Transition(state, output, sideEffects)
        }

        fun <State, Output> State.withOutput(output: Output?): Transition<State, Output> {
            return Transition(this, output)
        }

        fun <State, Output> State.withSideEffect(sideEffect: SideEffect?): Transition<State, Output> {
            val effects = sideEffect?.let {
                listOf(it)
            } ?: emptyList()

            return Transition(this, null, effects)
        }


    }
}
