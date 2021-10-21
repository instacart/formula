package com.instacart.formula.internal

import com.instacart.formula.BoundStream
import com.instacart.formula.Snapshot
import com.instacart.formula.Stream
import com.instacart.formula.StreamBuilder
import com.instacart.formula.Transition

/**
 * Implements [StreamBuilder] interface.
 */
internal class StreamBuilderImpl<out Input, State> internal constructor(
    private val snapshot: Snapshot<Input, State>,
) : StreamBuilder<Input, State>(
    input = snapshot.input,
    state = snapshot.state,
) {
    internal val boundedStreams = mutableListOf<BoundStream<*>>()

    override fun <Event> events(
        stream: Stream<Event>,
        transition: Transition<Input, State, Event>,
    ) {
        add(toBoundStream(stream, transition))
    }

    override fun <Event> onEvent(
        stream: Stream<Event>,
        avoidParameterClash: Any,
        transition: Transition<Input, State, Event>,
    ) {
        add(toBoundStream(stream, transition))
    }

    override fun <Event> Stream<Event>.onEvent(
        transition: Transition<Input, State, Event>,
    ) {
        val stream = this
        this@StreamBuilderImpl.events(stream, transition)
    }

    private fun add(connection: BoundStream<*>) {
        if (boundedStreams.contains(connection)) {
            throw IllegalStateException("duplicate stream with key: ${connection.keyAsString()}")
        }

        boundedStreams.add(connection)
    }

    private fun <Event> toBoundStream(
        stream: Stream<Event>,
        transition: Transition<Input, State, Event>,
    ): BoundStream<Event> {
        val key = JoinedKey(stream.key(), transition.type())
        val listener = snapshot.context.eventListener(key, transition)
        return BoundStream(
            key = key,
            stream = stream,
            initial = listener
        )
    }
}