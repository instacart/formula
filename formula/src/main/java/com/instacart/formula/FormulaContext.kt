package com.instacart.formula

import com.instacart.formula.internal.ScopedCallbacks
import io.reactivex.Observable

/**
 * This interface provides ability to [Formula] to trigger transitions, instantiate updates and create
 * child formulas.
 */
abstract class FormulaContext<State> internal constructor(
    @PublishedApi internal val callbacks: ScopedCallbacks
) {

    /**
     * Creates a callback to be used for handling UI event transitions.
     *
     * It uses inlined callback anonymous class for type.
     */
    inline fun callback(crossinline transition: Transition.Factory.() -> Transition<State>): () -> Unit {
        val callback: () -> Unit = {
            performTransition(transition(Transition.Factory))
        }
        val reference = callbacks.initOrFindCallback(callback::class)
        reference.callback = callback
        return reference
    }

    /**
     * Creates a callback to be used for handling UI event transitions.
     *
     * @param key Unique identifier that describes this callback
     */
    inline fun callback(
        key: String,
        crossinline transition: Transition.Factory.() -> Transition<State>
    ): () -> Unit {
        if (key.isBlank()) {
            throw IllegalStateException("Key cannot be blank.")
        }

        val callback = callbacks.initOrFindCallback(key)
        callback.callback = {
            performTransition(transition(Transition.Factory))
        }
        return callback
    }

    /**
     * Creates a callback that takes a [UIEvent] and performs a [Transition].
     *
     * It uses inlined callback anonymous class for type.
     */
    inline fun <UIEvent> eventCallback(
        crossinline transition: Transition.Factory.(UIEvent) -> Transition<State>
    ): (UIEvent) -> Unit {
        val callback: (UIEvent) -> Unit = {
            performTransition(transition(Transition.Factory, it))
        }

        val reference = callbacks.initOrFindEventCallback<UIEvent>(callback::class)
        reference.callback = callback
        return reference
    }

    /**
     * Creates a callback that takes a [UIEvent] and performs a [Transition].
     *
     * @param key Unique identifier that describes this callback
     */
    inline fun <UIEvent> eventCallback(
        key: String,
        crossinline transition: Transition.Factory.(UIEvent) -> Transition<State>
    ): (UIEvent) -> Unit {
        if (key.isBlank()) {
            throw IllegalStateException("Key cannot be blank.")
        }

        val callback = callbacks.initOrFindEventCallback<UIEvent>(key)
        callback.callback = {
            performTransition(transition(Transition.Factory, it))
        }
        return callback
    }

    /**
     * Starts building a child [Formula]. The state management of child [Formula]
     * will be managed by the runtime. Call [Child.input] to finish declaring the child
     * and receive the [ChildRenderModel].
     */
    fun <ChildInput, ChildState, ChildRenderModel> child(
        formula: Formula<ChildInput, ChildState, ChildRenderModel>
    ): Child<State, ChildInput, ChildRenderModel> {
        return child("", formula)
    }

    /**
     * Starts building a child [Formula]. The state management of child [Formula]
     * will be managed by the runtime. Call [Child.input] to finish declaring the child
     * and receive the [ChildRenderModel].
     *
     * @param key A unique identifier for this formula.
     */
    abstract fun <ChildInput, ChildState, ChildRenderModel> child(
        key: String,
        formula: Formula<ChildInput, ChildState, ChildRenderModel>
    ): Child<State, ChildInput, ChildRenderModel>

    /**
     * Provides an [UpdateBuilder] that enables [Formula] to declare various events and effects.
     */
    abstract fun updates(init: UpdateBuilder<State>.() -> Unit): List<Update<*, *>>

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

    @PublishedApi internal abstract fun performTransition(transition: Transition<State>)

    /**
     * Provides methods to declare various events and effects.
     */
    class UpdateBuilder<State>(
        @PublishedApi internal val transitionCallback: (Transition<State>) -> Unit
    ) {
        internal val updates = mutableListOf<Update<*, *>>()

        /**
         * Adds a [Stream] as part of this [Evaluation]. [Stream] will be subscribed when it is initially added
         * and unsubscribed when it is not returned as part of [Evaluation].
         *
         * @param stream An instance of [Stream].
         * @param input An object passed to the [Stream] for instantiation. This can
         * @param transition Callback invoked when [Stream] sends us a [Message].
         */
        inline fun <Input : Any, Message> events(
            stream: Stream<Input, Message>,
            input: Input,
            crossinline transition: Transition.Factory.(Message) -> Transition<State>
        ) {
            events("", stream, input, transition)
        }

        /**
         * Adds a [Stream] as part of this [Evaluation]. [Stream] will be subscribed when it is initially added
         * and unsubscribed when it is not returned as part of [Evaluation].
         *
         * @param key an extra parameter used to distinguish between different streams.
         * @param stream an instance of [Stream].
         * @param input An object passed to the [Stream] for instantiation. This can
         * @param transition Callback invoked when [Stream] sends us a [Message].
         */
        inline fun <Input : Any, Message> events(
            key: String,
            stream: Stream<Input, Message>,
            input: Input,
            crossinline transition: Transition.Factory.(Message) -> Transition<State>
        ) {
            add(createConnection(key, stream, input, transition))
        }

        /**
         * Adds a [Stream] as part of this [Evaluation]. [Stream] will be subscribed when it is initially added
         * and unsubscribed when it is not returned as part of [Evaluation].
         *
         * @param transition Callback invoked when [Stream] sends us a [Message].
         */
        inline fun <Message> events(
            stream: Stream<Unit, Message>,
            crossinline transition: Transition.Factory.(Message) -> Transition<State>
        ) {
            events("", stream, transition)
        }

        /**
         * Adds a [Stream] as part of this [Evaluation]. [Stream] will be subscribed when it is initially added
         * and unsubscribed when it is not returned as part of [Evaluation].
         *
         * @param key An extra parameter used to distinguish between streams.
         */
        inline fun <Message> events(
            key: String,
            stream: Stream<Unit, Message>,
            crossinline transition: Transition.Factory.(Message) -> Transition<State>
        ) {
            events(key, stream, Unit, transition)
        }

        /**
         * Adds an [Observable] as part of this [Evaluation]. Observable will be subscribed when it is initially added
         * and unsubscribed when it is not returned as part of [Evaluation].
         */
        inline fun <Message> events(
            observable: Observable<Message>,
            crossinline transition: Transition.Factory.(Message) -> Transition<State>
        ) {
            events("", RxStream.fromObservable { observable }, transition)
        }

        /**
         * Adds an [Observable] as part of this [Evaluation]. Observable will be subscribed when it is initially added
         * and unsubscribed when it is not returned as part of [Evaluation].
         *
         * @param key Primary way to distinguish between different observables.
         */
        inline fun <Message> events(
            key: String,
            observable: Observable<Message>,
            crossinline transition: Transition.Factory.(Message) -> Transition<State>
        ) {
            events(key, RxStream.fromObservable { observable }, transition)
        }

        @PublishedApi internal fun add(connection: Update<*, *>) {
            if (updates.contains(connection)) {
                throw IllegalStateException("duplicate stream with key: ${connection.keyAsString()}")
            }

            updates.add(connection)
        }

        @PublishedApi internal inline fun <Input : Any, Message> createConnection(
            key: Any? = null,
            stream: Stream<Input, Message>,
            input: Input,
            crossinline transition: Transition.Factory.(Message) -> Transition<State>
        ): Update<Input, Message> {
            val callback: (Message) -> Unit = {
                val value = transition(Transition.Factory, it)
                transitionCallback(value)
            }

            return Update(
                key = Update.Key(input, callback::class, key),
                input = input,
                stream = stream,
                initial = callback
            )
        }
    }
}
