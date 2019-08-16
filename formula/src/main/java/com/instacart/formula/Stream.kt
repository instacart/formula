package com.instacart.formula

/**
 * A [Stream] defines an asynchronous event(s).
 *
 * @param Input Type of Input that is used to initialize a stream. Use [Unit] if stream doesn't need any input.
 * @param Message A type of messages that the stream produces.
 */
interface Stream<Input, Message> {
    companion object {

        /**
         * Emits a message when [Stream] is initialized. Use this stream to send a message when [Formula]
         * is initialized.
         * ```
         * events(Stream.onInit()) {
         *   message(analytics::trackClose)
         * }
         */
        fun onInit(): Stream<Unit, Unit> {
            @Suppress("UNCHECKED_CAST")
            return StartMessageStream as Stream<Unit, Unit>
        }

        /**
         * Emits a message when [Stream] is initialized or [Input] has changed. Use this stream to send a message
         * with latest [Input] value.
         * ```
         * events(Stream.onInput(), itemId) {
         *   message(api::fetchItem, itemId)
         * }
         * ```
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
            return CancelMessageStream
        }
    }

    /**
     * This method is called when Stream is first declared within [Formula].
     *
     * @param send Use this callback to pass messages back to [Formula].
     *             Note: you need to call this on the main thread.
     */
    fun start(input: Input, send: (Message) -> Unit): Cancelable?
}

/**
 * Emits a message as soon as [Stream] is initialized.
 */
internal object StartMessageStream : Stream<Any, Any> {
    override fun start(input: Any, send: (Any) -> Unit): Cancelable? {
        send(input)
        return null
    }
}

/**
 * Emits a message when [Stream] is canceled.
 */
internal object CancelMessageStream : Stream<Unit, Unit> {
    override fun start(input: Unit, send: (Unit) -> Unit): Cancelable? {
        return Cancelable {
            send(input)
        }
    }
}


