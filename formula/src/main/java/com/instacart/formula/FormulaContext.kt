package com.instacart.formula

import com.instacart.formula.internal.JoinedKey
import com.instacart.formula.internal.ScopedListeners
import com.instacart.formula.internal.TransitionDispatcher
import com.instacart.formula.internal.UnitListener

/**
 * Provides functionality within [evaluate][Formula.evaluate] function to [compose][child]
 * child formulas, handle events [FormulaContext.onEvent], and [respond][FormulaContext.updates]
 * to arbitrary asynchronous events.
 */
abstract class FormulaContext<State> internal constructor(
    @PublishedApi internal val listeners: ScopedListeners<State>,
    internal val transitionDispatcher: TransitionDispatcher<State>,
) {

    /**
     * Creates a [Listener] that takes an [Event] and performs a [Transition].
     *
     * It uses [transition] type as key.
     */
    fun <Event> onEvent(
        transition: Transition.Factory.(Event) -> Transition<State>,
    ): Listener<Event> {
        return onEvent(key = transition::class, transition)
    }

    /**
     * Creates a [Listener] that takes a [Event] and performs a [Transition].
     *
     * @param key key with which the listener is to be associated. Same key cannot be used for multiple listeners.
     */
    fun <Event> onEvent(
        key: Any,
        transition: Transition.Factory.(Event) -> Transition<State>,
    ): Listener<Event> {
        val listener = listeners.initOrFindListener<Event>(key)
        listener.transitionDispatcher = transitionDispatcher
        listener.transition = transition
        return listener
    }

    /**
     * Creates a listener that takes an event and performs a [Transition].
     *
     * It uses [transition] type as key.
     */
    fun callback(transition: Transition.Factory.(Unit) -> Transition<State>): () -> Unit {
        val event = onEvent(transition)
        return UnitListener(event)
    }

    /**
     * Creates a listener that takes an event and performs a [Transition].
     *
     * @param key key with which the listener is to be associated. Same key cannot be used for multiple listeners.
     */
    fun callback(
        key: Any,
        transition: Transition.Factory.(Unit) -> Transition<State>
    ): () -> Unit {
        val event = onEvent(key, transition)
        return UnitListener(event)
    }

    /**
     * Creates a listener that takes a [Event] and performs a [Transition].
     *
     * It uses [transition] type as key.
     */
    fun <Event> eventCallback(
        transition: Transition.Factory.(Event) -> Transition<State>
    ): Listener<Event> {
        return onEvent(transition)
    }

    /**
     * Creates a listener that takes a [Event] and performs a [Transition].
     *
     * @param key key with which the listener is to be associated. Same key cannot be used for multiple listeners.
     */
    fun <Event> eventCallback(
        key: Any,
        transition: Transition.Factory.(Event) -> Transition<State>,
    ): Listener<Event> {
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
        listeners.enterScope(key)
        val value = create()
        listeners.endScope()
        return value
    }

    /**
     * Provides methods to declare various events and effects.
     */
    class UpdateBuilder<State> internal constructor(
        private val formulaContext: FormulaContext<State>,
    ) {
        internal val updates = mutableListOf<BoundStream<*>>()

        /**
         * Adds a [Stream] as part of this [Evaluation]. [Stream] will be subscribed when it is initially added
         * and unsubscribed when it is not returned as part of [Evaluation].
         *
         * @param transition A function that is invoked when [Stream] sends us a [Message].
         */
        fun <Message> events(
            stream: Stream<Message>,
            transition: Transition.Factory.(Message) -> Transition<State>,
        ) {
            add(createConnection(stream, transition))
        }

        /**
         * Adds a [Stream] as part of this [Evaluation]. [Stream] will be subscribed when it is initially added
         * and unsubscribed when it is not returned as part of [Evaluation].
         *
         * @param transition A function that is invoked when [Stream] sends us a [Message].
         */
        fun <Message> onEvent(
            stream: Stream<Message>,
            avoidParameterClash: Any = this,
            transition: Transition.Factory.(Message) -> Transition<State>,
        ) {
            add(createConnection(stream, transition))
        }

        /**
         * Adds a [Stream] as part of this [Evaluation]. [Stream] will be subscribed when it is initially added
         * and unsubscribed when it is not returned as part of [Evaluation].
         *
         * @param transition A function that is invoked when [Stream] sends us a [Message].
         *
         * Example:
         * ```
         * Stream.onInit().onEvent {
         *   transition { /* */ }
         * }
         * ```
         */
        fun <Message> Stream<Message>.onEvent(
            transition: Transition.Factory.(Message) -> Transition<State>,
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
            transition: Transition.Factory.(Message) -> Transition<State>,
        ): BoundStream<Message> {
            val key = JoinedKey(stream.key(), transition::class)
            val listener = formulaContext.onEvent(key, transition)
            return BoundStream(
                key = key,
                stream = stream,
                initial = listener
            )
        }
    }
}