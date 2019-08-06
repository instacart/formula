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
         * Creates a transition to a new [State].
         */
        fun <State> transition(state: State): Transition<State> {
            return Transition(state)
        }

        /**
         * Creates a transition to a new [State] and emits messages added within [buildMessages] block.
         */
        inline fun <State> transition(
            state: State? = null,
            buildMessages: MessageBuilder.() -> Unit
        ): Transition<State> {
            val messages = MessageBuilder().apply(buildMessages).list
            return Transition(state, messages = messages)
        }

        /**
         * Creates a transition to a new [State] with no additional messages.
         */
        fun <State> State.noMessages(): Transition<State> {
            return Transition(this)
        }

        /**
         * Creates a transition to a new [State] with an optional message.
         */
        fun <State> State.withMessage(invocation: (() -> Unit)?): Transition<State> {
            val effects = invocation
                ?.let { listOf(UnitMessage(it)) }
                ?: emptyList()

            return Transition(this, messages = effects)
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

        inline fun <State> State.withMessages(build: MessageBuilder.() -> Unit): Transition<State> {
            val messages = MessageBuilder().apply(build).list
            return Transition(this, messages = messages)
        }
    }
}
