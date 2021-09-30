package com.instacart.formula

import com.instacart.formula.internal.JoinedKey
import com.instacart.formula.internal.ScopedCallbacks
import com.instacart.formula.internal.TransitionDispatcher

/**
 * Provides functionality within [evaluate][Formula.evaluate] function to [compose][child]
 * child formulas, handle events [FormulaContext.onEvent], and [respond][FormulaContext.updates]
 * to arbitrary asynchronous events.
 */
abstract class FormulaContext<State> internal constructor(
    @PublishedApi internal val callbacks: ScopedCallbacks,
    @PublishedApi internal val transitionDispatcher: TransitionDispatcher<State>,
) {

    /**
     * Creates a callback that takes a [Event] and performs a [Transition].
     *
     * It uses inlined callback anonymous class for type.
     */
    fun <Event> onEvent(
        transition: UpdateType<State, Event>,
    ): Listener<Event> {
        val reference = callbacks.initOrFindEventCallback<Event>(transition::class)
        reference.delegate = transitionDispatcher.toCallback(transition)
        return reference
    }

    /**
     * Creates a callback that takes a [Event] and performs a [Transition].
     *
     * @param key key with which the callback is to be associated. Same key cannot be used for multiple callbacks.
     */
    fun <Event> onEvent(
        key: Any,
        transition: UpdateType<State, Event>,
    ): Listener<Event> {
        val callback = callbacks.initOrFindEventCallback<Event>(key)
        callback.delegate = transitionDispatcher.toCallback(transition)
        return callback
    }

    /**
     * Creates a callback to be used for handling UI event transitions.
     *
     * It uses inlined callback anonymous class for type.
     */
    fun callback(transition: UpdateType<State, Unit>): () -> Unit {
        val reference = callbacks.initOrFindCallback(transition::class)
        reference.delegate = transitionDispatcher.toCallback(transition)
        return reference
    }

    /**
     * Creates a callback to be used for handling UI event transitions.
     *
     * @param key key with which the callback is to be associated. Same key cannot be used for multiple callbacks.
     */
    fun callback(
        key: Any,
        transition: UpdateType<State, Unit>
    ): () -> Unit {
        val callback = callbacks.initOrFindCallback(key)
        callback.delegate = transitionDispatcher.toCallback(transition)
        return callback
    }

    /**
     * Creates a callback that takes a [Event] and performs a [Transition].
     *
     * It uses inlined callback anonymous class for type.
     */
    fun <Event> eventCallback(
        transition: UpdateType<State, Event>,
    ): Listener<Event> {
        return onEvent(transition)
    }

    /**
     * Creates a callback that takes a [Event] and performs a [Transition].
     *
     * @param key key with which the callback is to be associated. Same key cannot be used for multiple callbacks.
     */
    fun <Event> eventCallback(
        key: Any,
        transition: UpdateType<State, Event>,
    ): (Event) -> Unit {
        return onEvent(key, transition)
    }

    /**
     * A convenience method to run a formula that takes no input. Returns the latest output
     * of the [child] formula. Formula runtime ensures the [child] is running, manages
     * its internal state and will trigger `evaluate` if needed.
     */
    fun <ChildOutput> child(
        child: IFormula<Unit, ChildOutput>
    ): ChildOutput {
        return child(child, Unit)
    }

    /**
     * Returns the latest output of the [child] formula. Formula runtime ensures the [child]
     * is running, manages its internal state and will trigger `evaluate` if needed.
     */
    abstract fun <ChildInput, ChildOutput> child(
        formula: IFormula<ChildInput, ChildOutput>,
        input: ChildInput
    ): ChildOutput

    /**
     * Provides an [UpdateBuilder] that enables [Formula] to declare various events and effects.
     */
    abstract fun updates(init: UpdateBuilder<State>.() -> Unit): List<BoundStream<*>>

    /**
     * Scopes [create] block with a [key].
     *
     * @param key Unique identifier that will be used for this block.
     */
    inline fun <Value> key(key: Any, create: () -> Value): Value {
        callbacks.enterScope(key)
        val value = create()
        callbacks.endScope()
        return value
    }

    /**
     * Provides methods to declare various events and effects.
     */
    class UpdateBuilder<State> internal constructor(
        @PublishedApi internal val transitionDispatcher: TransitionDispatcher<State>
    ) {
        internal val updates = mutableListOf<BoundStream<*>>()

        /**
         * Adds a [Stream] as part of this [Evaluation]. [Stream] will be subscribed when it is initially added
         * and unsubscribed when it is not returned as part of [Evaluation].
         *
         * @param transition Callback invoked when [Stream] sends us a [Message].
         */
        fun <Message> events(
            stream: Stream<Message>,
            transition: UpdateType<State, Message>,
        ) {
            add(createConnection(stream, transition))
        }

        /**
         * Adds a [Stream] as part of this [Evaluation]. [Stream] will be subscribed when it is initially added
         * and unsubscribed when it is not returned as part of [Evaluation].
         *
         * @param transition Callback invoked when [Stream] sends us a [Message].
         */
        fun <Message> onEvent(
            stream: Stream<Message>,
            avoidParameterClash: Any = this,
            transition: UpdateType<State, Message>,
        ) {
            add(createConnection(stream, transition))
        }

        /**
         * Adds a [Stream] as part of this [Evaluation]. [Stream] will be subscribed when it is initially added
         * and unsubscribed when it is not returned as part of [Evaluation].
         *
         * @param transition Callback invoked when [Stream] sends us a [Message].
         *
         * Example:
         * ```
         * Stream.onInit().onEvent {
         *   transition { /* */ }
         * }
         * ```
         */
        fun <Message> Stream<Message>.onEvent(
            transition: UpdateType<State, Message>,
        ) {
            val stream = this
            this@UpdateBuilder.events(stream, transition)
        }

        @PublishedApi internal fun add(connection: BoundStream<*>) {
            if (updates.contains(connection)) {
                throw IllegalStateException("duplicate stream with key: ${connection.keyAsString()}")
            }

            updates.add(connection)
        }

        @PublishedApi internal fun <Message> createConnection(
            stream: Stream<Message>,
            transition: UpdateType<State, Message>,
        ): BoundStream<Message> {
            val callback = transitionDispatcher.toCallback(transition)
            return BoundStream(
                key = JoinedKey(stream.key(), transition::class),
                stream = stream,
                initial = callback
            )
        }
    }
}