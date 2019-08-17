package com.instacart.formula

/**
 * A [Stream] defines an asynchronous event(s).
 *
 * @param Data Type of data that is used to initialize a stream. Use [Unit] if stream doesn't need any data.
 * @param Message A type of messages that the stream produces.
 */
interface Stream<Data, Message> {
    companion object {

        /**
         * Emits a message when [Stream] is initialized. Use this stream to send a message when [Formula]
         * is initialized.
         * ```
         * events(Stream.onInit()) {
         *   message(analytics::trackViewEvent)
         * }
         */
        fun onInit(): Stream<Unit, Unit> {
            @Suppress("UNCHECKED_CAST")
            return StartMessageStream as Stream<Unit, Unit>
        }

        /**
         * Emits a message when [Stream] is initialized or [Data] has changed. Use this stream to send a message
         * with latest [Data] value.
         * ```
         * events(Stream.onData(), itemId) {
         *   message(api::fetchItem, itemId)
         * }
         * ```
         */
        fun <Data> onData(): Stream<Data, Data> {
            @Suppress("UNCHECKED_CAST")
            return StartMessageStream as Stream<Data, Data>
        }

        /**
         * Emits a message when [Formula] is terminated.
         * ```
         * events(Stream.onTerminate()) {
         *   message(analytics::trackCloseEvent)
         * }
         * ```
         *
         * Note that transitions to new state will be discarded because [Formula] is terminated. This is best to
         * use to notify other services/analytics of [Formula] termination.
         */
        fun onTerminate(): Stream<Unit, Unit> {
            return TerminateMessageStream
        }
    }

    /**
     * This method is called when Stream is first declared within [Formula].
     *
     * @param send Use this callback to pass messages back to [Formula].
     *             Note: you need to call this on the main thread.
     */
    fun start(input: Data, send: (Message) -> Unit): Cancelable?
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
 * Emits a message when [Formula] is terminated.
 */
internal object TerminateMessageStream : Stream<Unit, Unit> {
    override fun start(input: Unit, send: (Unit) -> Unit): Cancelable? {
        return Cancelable {
            send(input)
        }
    }
}


