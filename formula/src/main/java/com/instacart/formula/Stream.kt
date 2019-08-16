package com.instacart.formula

/**
 * A [Stream] defines an asynchronous event(s).
 *
 * @param Input - Type of Input that is used to initialize a stream. Use [Unit] if stream doesn't need any input.
 * @param Message - A type of messages that the stream produces.
 */
interface Stream<Input, Message> {
    companion object {

        /**
         * Emits a message when [Stream] is initialized.
         */
        fun onInit(): Stream<Unit, Unit> {
            @Suppress("UNCHECKED_CAST")
            return StartMessageStream as Stream<Unit, Unit>
        }

        /**
         * Emits a message when [Stream] is initialized or [Input] has changed.
         */
        fun <Input> onInput(): Stream<Input, Input> {
            @Suppress("UNCHECKED_CAST")
            return StartMessageStream as Stream<Input, Input>
        }

        /**
         * Emits a message when [Stream] is canceled. Use this stream to send a message when [Formula] is removed.
         * ```
         * events(Stream.onCancel()) {
         *   message(analytics::trackClose)
         * }
         * ```
         */
        fun onCancel(): Stream<Unit, Unit> {
            @Suppress("UNCHECKED_CAST")
            return CancelMessageStream as Stream<Unit, Unit>
        }
    }

    fun start(input: Input, onEvent: (Message) -> Unit): Cancelation?
}

/**
 * Triggers [onEvent] as soon as [start] is called.
 */
internal object StartMessageStream : Stream<Any, Any> {
    override fun start(input: Any, onEvent: (Any) -> Unit): Cancelation? {
        onEvent(input)
        return null
    }
}

/**
 * Triggers [onEvent] when [Formula] is removed.
 */
internal object CancelMessageStream : Stream<Any, Any> {
    override fun start(input: Any, onEvent: (Any) -> Unit): Cancelation? {
        return Cancelation {
            onEvent(input)
        }
    }
}


