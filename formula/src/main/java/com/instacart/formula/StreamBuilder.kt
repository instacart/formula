package com.instacart.formula

import com.instacart.formula.internal.JoinedKey

/**
 * Stream builder is used to declare [streams][Stream] within Formula
 * [evaluation][Formula.evaluate]. Call [FormulaContext.updates] to start
 * the process and use [events] or [onEvent] to provide a [Transition]
 * which will be called when stream emits an event/
 */
class StreamBuilder<out Input, State> internal constructor(
    private val formulaContext: FormulaContext<Input, State>,
) {
    internal val updates = mutableListOf<BoundStream<*>>()

    /**
     * Adds a [Stream] as part of this [Evaluation]. [Stream] will be subscribed when it is initially added
     * and unsubscribed when it is not returned as part of [Evaluation].
     *
     * @param transition A function that is invoked when [Stream] produces an [Event].
     */
    fun <Event> events(
        stream: Stream<Event>,
        transition: Transition<Input, State, Event>,
    ) {
        add(createConnection(stream, transition))
    }

    /**
     * Adds a [Stream] as part of this [Evaluation]. [Stream] will be subscribed when it is initially added
     * and unsubscribed when it is not returned as part of [Evaluation].
     *
     * @param transition A function that is invoked when [Stream] produces an [Event].
     */
    fun <Event> onEvent(
        stream: Stream<Event>,
        avoidParameterClash: Any = this,
        transition: Transition<Input, State, Event>,
    ) {
        add(createConnection(stream, transition))
    }

    /**
     * Adds a [Stream] as part of this [Evaluation]. [Stream] will be subscribed when it is initially added
     * and unsubscribed when it is not returned as part of [Evaluation].
     *
     * @param transition A function that is invoked when [Stream] produces an [Event].
     *
     * Example:
     * ```
     * Stream.onInit().onEvent {
     *   transition { /* */ }
     * }
     * ```
     */
    fun <Event> Stream<Event>.onEvent(
        transition: Transition<Input, State, Event>,
    ) {
        val stream = this
        this@StreamBuilder.events(stream, transition)
    }

    @PublishedApi internal fun add(connection: BoundStream<*>) {
        if (updates.contains(connection)) {
            throw IllegalStateException("duplicate stream with key: ${connection.keyAsString()}")
        }

        updates.add(connection)
    }

    @PublishedApi internal fun <Event> createConnection(
        stream: Stream<Event>,
        transition: Transition<Input, State, Event>,
    ): BoundStream<Event> {
        val key = JoinedKey(stream.key(), transition.type())
        val listener = formulaContext.eventListener(key, transition)
        return BoundStream(
            key = key,
            stream = stream,
            initial = listener
        )
    }
}