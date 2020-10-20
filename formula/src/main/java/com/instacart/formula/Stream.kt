package com.instacart.formula

/**
 * A [Stream] defines an asynchronous event(s).
 *
 * To use it within a [Formula]:
 * ```
 * Evaluation(
 *   updates = context.updates {
 *     events(stream) {
 *       transition()
 *     }
 *   }
 * )
 * ```
 *
 * @param Message A type of messages that the stream produces.
 */
interface Stream<Message> {
    companion object {

        /**
         * Emits a message when [Stream] is initialized. Use this stream to send effects when [Formula]
         * is initialized.
         * ```
         * events(Stream.onInit()) {
         *   transition { analytics.trackViewEvent() }
         * }
         */
        fun onInit(): Stream<Unit> {
            @Suppress("UNCHECKED_CAST")
            return StartMessageStream(Unit)
        }

        /**
         * Emits a message with [data] when [Stream] is initialized. Uses [data] as key.
         *
         * Use this stream to send a effects with latest [Data] value.
         * ```
         * events(Stream.onData(itemId)) {
         *   transition { api.fetchItem(itemId) }
         * }
         * ```
         */
        fun <Data : Any> onData(data: Data): Stream<Data> {
            return StartMessageStream(data)
        }

        /**
         * Emits a message when [Formula] is terminated.
         * ```
         * events(Stream.onTerminate()) {
         *   transition { analytics.trackCloseEvent() }
         * }
         * ```
         *
         * Note that transitions to new state will be discarded because [Formula] is terminated. This is best to
         * use to notify other services/analytics of [Formula] termination.
         */
        fun onTerminate(): Stream<Unit> {
            return TerminateMessageStream
        }
    }

    /**
     * This method is called when Stream is first declared within [Formula].
     *
     * @param send Use this callback to pass messages back to [Formula].
     *             Note: you need to call this on the main thread.
     */
    fun start(send: (Message) -> Unit): Cancelable?

    /**
     * Used to distinguish between different types of Streams.
     */
    fun key(): Any
}

/**
 * Emits a message as soon as [Stream] is initialized.
 */
internal class StartMessageStream<Data : Any>(
    private val data: Data
) : Stream<Data> {

    override fun start(send: (Data) -> Unit): Cancelable? {
        send(data)
        return null
    }

    override fun key(): Any = data
}

/**
 * Emits a message when [Formula] is terminated.
 */
internal object TerminateMessageStream : Stream<Unit> {
    override fun start(send: (Unit) -> Unit): Cancelable? {
        return Cancelable {
            send(Unit)
        }
    }

    override fun key(): Any = Unit
}