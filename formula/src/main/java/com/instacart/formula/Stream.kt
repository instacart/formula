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
 * @param Event A type of event that the stream produces.
 */
interface Stream<Event> {
    companion object {

        /**
         * Emits an event when [Stream] is initialized. Use this stream to send effects when [Formula]
         * is initialized.
         * ```
         * events(Stream.onInit()) {
         *   transition { analytics.trackViewEvent() }
         * }
         */
        fun onInit(): Stream<Unit> {
            @Suppress("UNCHECKED_CAST")
            return StartEventStream(Unit)
        }

        /**
         * Emits a [data] event when [Stream] is initialized. Uses [data] as key.
         *
         * Use this stream to send a effects with latest [Data] value.
         * ```
         * events(Stream.onData(itemId)) {
         *   transition { api.fetchItem(itemId) }
         * }
         * ```
         */
        fun <Data> onData(data: Data): Stream<Data> {
            return StartEventStream(data)
        }

        /**
         * Emits an event when [Formula] is terminated.
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
            return TerminateEventStream
        }
    }

    /**
     * This method is called when Stream is first declared within [Formula].
     *
     * @param send Use this listener to send events back to [Formula].
     *             Note: you need to call this on the main thread.
     */
    fun start(send: (Event) -> Unit): Cancelable?

    /**
     * Used to distinguish between different types of Streams.
     */
    fun key(): Any?
}

/**
 * Emits an event as soon as [Stream] is initialized.
 */
internal class StartEventStream<Data>(
    private val data: Data
) : Stream<Data> {

    override fun start(send: (Data) -> Unit): Cancelable? {
        send(data)
        return null
    }

    override fun key(): Any? = data
}

/**
 * Emits an event when [Formula] is terminated.
 */
internal object TerminateEventStream : Stream<Unit> {
    override fun start(send: (Unit) -> Unit): Cancelable {
        return Cancelable {
            send(Unit)
        }
    }

    override fun key(): Any = Unit
}