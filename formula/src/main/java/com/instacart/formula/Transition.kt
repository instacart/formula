package com.instacart.formula

/**
 * Defines an intent to transition by emitting a new [State] and 0..N number of messages.
 *
 * @param state Updated state
 * @param messages Optional messages such as parent callbacks / logging / db writes/ network requests / etc.
 */
data class Transition<out State> @PublishedApi internal constructor(
    val state: State? = null,
    val messages: List<Message> = emptyList()
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
        private val NONE = Transition<Any>()

        /**
         * A transition that does nothing.
         */
        fun <State> none(): Transition<State> {
            @Suppress("UNCHECKED_CAST")
            return NONE as Transition<State>
        }

        /**
         * Creates a transition to a new [State] and emits messages added within [buildMessages] block.
         */
        inline fun <State> transition(
            state: State? = null,
            buildMessages: MessageBuilder.() -> Unit
        ): Transition<State> {
            val messages = MessageBuilder().apply(buildMessages).list
            return Transition(state, messages)
        }

        /**
         * Creates a transition that has an optional state change and 0..N messages.
         */
        fun <State> transition(
            state: State? = null,
            messages: List<Message> = emptyList()
        ): Transition<State> {
            return Transition(state, messages)
        }

        /**
         * Creates a transition to a new [State] with no additional messages.
         */
        fun <State> State.noMessages(): Transition<State> {
            return transition(this)
        }

        /**
         * Creates a transition to a new [State] with an optional message.
         */
        fun <State> State.withMessage(invocation: (() -> Unit)?): Transition<State> {
            val messages = invocation
                ?.let { listOf(UnitMessage(it)) }
                ?: emptyList()

            return Transition(this, messages = messages)
        }

        /**
         * Creates a transition to a new [State] with a [Data] message.
         */
        fun <State, Data> State.withMessage(invocation: (Data) -> Unit, data: Data): Transition<State> {
            val messages = listOf(EventMessage(invocation, data))
            return Transition(this, messages = messages)
        }

        /**
         * Creates a transition that emits a single [Message] with no data.
         */
        fun <State> message(handler: () -> Unit): Transition<State> {
            return Transition(messages = listOf(UnitMessage(handler)))
        }

        /**
         * Creates a transition that emits a single [Message] with [Data].
         */
        fun <State, Data> message(handler: (Data) -> Unit, data: Data): Transition<State> {
            return Transition(messages = listOf(EventMessage(handler, data)))
        }

        /**
         * Creates a transition that emits messages added within [buildMessages] block.
         */
        inline fun <State> messages(buildMessages: MessageBuilder.() -> Unit): Transition<State> {
            val messages = MessageBuilder().apply(buildMessages).list
            return Transition(messages = messages)
        }

        inline fun <State> State.withMessages(build: MessageBuilder.() -> Unit): Transition<State> {
            val messages = MessageBuilder().apply(build).list
            return Transition(this, messages = messages)
        }
    }
}
