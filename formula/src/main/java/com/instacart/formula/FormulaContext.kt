package com.instacart.formula

import com.instacart.formula.internal.Callback
import com.instacart.formula.internal.EventCallback
import io.reactivex.Observable

/**
 * This interface provides ability to [Formula] to trigger transitions, instantiate updates and create
 * child formulas.
 */
abstract class FormulaContext<State, Output> {

    /**
     * Creates a callback to be used for handling UI event transitions.
     *
     * NOTE: this uses positional index to determine the key.
     */
    inline fun callback(crossinline transition: Transition.Factory.() -> Transition<State, Output>): () -> Unit {
        val callback = initOrFindPositionalCallback()
        callback.callback = {
            performTransition(transition(Transition.Factory))
        }
        return callback
    }

    /**
     * Creates a callback if [condition] is true.
     */
    inline fun optionalCallback(
        condition: Boolean,
        crossinline transition: Transition.Factory.() -> Transition<State, Output>
    ): (() -> Unit)? {
        return initOrFindOptionalCallback(condition)?.apply {
            callback = {
                performTransition(transition(Transition.Factory))
            }
        }
    }

    /**
     * Creates a callback to be used for handling UI event transitions.
     *
     * @param key Unique identifier that describes this callback
     */
    inline fun callback(
        key: String,
        crossinline transition: Transition.Factory.() -> Transition<State, Output>
    ): () -> Unit {
        val callback = initOrFindCallback(key)
        callback.callback = {
            performTransition(transition(Transition.Factory))
        }
        return callback
    }

    /**
     * Creates a callback that takes a [UIEvent] and performs a [Transition].
     *
     * NOTE: this uses positional index to determine the key.
     */
    inline fun <UIEvent> eventCallback(
        crossinline transition: Transition.Factory.(UIEvent) -> Transition<State, Output>
    ): (UIEvent) -> Unit {
        val callback = initOrFindPositionalEventCallback<UIEvent>()
        callback.callback = {
            performTransition(transition(Transition.Factory, it))
        }
        return callback
    }

    /**
     * If [condition] is met, creates a callback that takes a [UIEvent] and performs a [Transition].
     *
     * NOTE: this uses positional index to determine the key.
     */
    inline fun <UIEvent> optionalEventCallback(
        condition: Boolean,
        crossinline transition: Transition.Factory.(UIEvent) -> Transition<State, Output>
    ): ((UIEvent) -> Unit)? {
        return initOrFindOptionalEventCallback<UIEvent>(condition)?.apply {
            callback = {
                performTransition(transition(Transition.Factory, it))
            }
        }
    }

    /**
     * Creates a callback that takes a [UIEvent] and performs a [Transition].
     *
     * @param key Unique identifier that describes this callback
     */
    inline fun <UIEvent> eventCallback(
        key: String,
        crossinline transition: Transition.Factory.(UIEvent) -> Transition<State, Output>
    ): (UIEvent) -> Unit {
        val callback = initOrFindEventCallback<UIEvent>(key)
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
    fun <ChildInput, ChildState, ChildOutput, ChildRenderModel> child(
        formula: Formula<ChildInput, ChildState, ChildOutput, ChildRenderModel>
    ): Child<State, Output, ChildInput, ChildOutput, ChildRenderModel> {
        return child("", formula)
    }

    /**
     * Starts building a child [Formula]. The state management of child [Formula]
     * will be managed by the runtime. Call [Child.input] to finish declaring the child
     * and receive the [ChildRenderModel].
     *
     * @param key A unique identifier for this formula.
     */
    abstract fun <ChildInput, ChildState, ChildOutput, ChildRenderModel> child(
        key: String,
        formula: Formula<ChildInput, ChildState, ChildOutput, ChildRenderModel>
    ): Child<State, Output, ChildInput, ChildOutput, ChildRenderModel>

    /**
     * Provides an [UpdateBuilder] that enables [Formula] to declare various events and effects.
     */
    abstract fun updates(init: UpdateBuilder<State, Output>.() -> Unit): List<Update>

    @PublishedApi internal abstract fun performTransition(transition: Transition<State, Output>)

    @PublishedApi internal abstract fun initOrFindPositionalCallback(): Callback

    @PublishedApi internal abstract fun initOrFindCallback(key: String): Callback

    @PublishedApi internal abstract fun initOrFindOptionalCallback(condition: Boolean): Callback?

    @PublishedApi internal abstract fun <UIEvent> initOrFindPositionalEventCallback(): EventCallback<UIEvent>

    @PublishedApi internal abstract fun <UIEvent> initOrFindEventCallback(key: String): EventCallback<UIEvent>

    @PublishedApi internal abstract fun <UIEvent> initOrFindOptionalEventCallback(condition: Boolean): EventCallback<UIEvent>?

    /**
     * Provides methods to declare various events and effects.
     */
    class UpdateBuilder<State, Output>(
        private val transitionCallback: (Transition<State, Output>) -> Unit
    ) {
        internal val updates = mutableListOf<Update>()

        /**
         * Adds a [Stream] as part of this [Evaluation]. [Stream] will be subscribed when it is initially added
         * and unsubscribed when it is not returned as part of [Evaluation].
         *
         * @param stream - an instance of [Stream]. The [Stream] class type will be used as a key. If you are declaring multiple streams of same type, also use [key].
         * @param input An object passed to the [Stream] for instantiation. This can
         * @param onEvent - a callback invoked when [Stream] produces an
         */
        fun <StreamInput : Any, StreamOutput> events(
            stream: Stream<StreamInput, StreamOutput>,
            input: StreamInput,
            onEvent: Transition.Factory.(StreamOutput) -> Transition<State, Output>
        ) {
            events("", stream, input, onEvent)
        }

        /**
         * Adds a [Stream] as part of this [Evaluation]. [Stream] will be subscribed when it is initially added
         * and unsubscribed when it is not returned as part of [Evaluation].
         *
         * @param stream - an instance of [Stream]. The [Stream] class type will be used as a key. If you are declaring multiple streams of same type, also use [key].
         * @param input An object passed to the [Stream] for instantiation. This can
         * @param key - an extra parameter used to distinguish between different streams.
         * @param onEvent - a callback invoked when [Stream] produces an
         */
        fun <StreamInput : Any, StreamOutput> events(
            key: String = "",
            stream: Stream<StreamInput, StreamOutput>,
            input: StreamInput,
            onEvent: Transition.Factory.(StreamOutput) -> Transition<State, Output>
        ) {
            add(createConnection(key, stream, input, onEvent))
        }

        /**
         * Adds a [Stream] as part of this [Evaluation]. [Stream] will be subscribed when it is initially added
         * and unsubscribed when it is not returned as part of [Evaluation].
         */
        fun <StreamOutput> events(
            stream: Stream<Unit, StreamOutput>,
            onEvent: Transition.Factory.(StreamOutput) -> Transition<State, Output>
        ) {
            events("", stream, onEvent)
        }

        /**
         * Adds a [Stream] as part of this [Evaluation]. [Stream] will be subscribed when it is initially added
         * and unsubscribed when it is not returned as part of [Evaluation].
         */
        fun <StreamOutput> events(
            key: String,
            stream: Stream<Unit, StreamOutput>,
            onEvent: Transition.Factory.(StreamOutput) -> Transition<State, Output>
        ) {
            events(key, stream, Unit, onEvent)
        }

        /**
         * Adds an [Observable] as part of this [Evaluation]. Observable will be subscribed when it is initially added
         * and unsubscribed when it is not returned as part of [Evaluation].
         *
         * @param key Primary way to distinguish between different observables.
         */
        fun <StreamOutput> events(
            key: String,
            observable: Observable<StreamOutput>,
            onEvent: Transition.Factory.(StreamOutput) -> Transition<State, Output>
        ) {
            val stream = object : RxStream<Unit, StreamOutput> {
                override fun observable(input: Unit): Observable<StreamOutput> {
                    return observable
                }
            }

            events(key, stream, onEvent)
        }

        /**
         * Define a side effect for which the uniqueness is tied only to [key]. It will be invoked once when it is initially added.
         *
         * @param key Used to distinguish between different types of effects.
         * @param action A callback that will be invoked once.
         */
        fun effect(key: String, action: () -> Unit) {
            val connection = Update.Effect(
                input = Unit,
                key = key,
                action = action
            )

            add(connection)
        }

        /**
         * Define a side effect for which the uniqueness is tied to [key] and [input]. It will be invoked once when it is initially added.
         *
         * @param key Used to distinguish between different types of effects.
         * @param input Will be passed to [action]. It is also used as key to distinguish different types of effects.
         * @param action A callback that will be invoked once.
         */
        fun <EffectInput : Any> effect(key: String, input: EffectInput, action: (EffectInput) -> Unit) {
            val connection = Update.Effect(
                input = input,
                key = key,
                action = {
                    action(input)
                }
            )

            add(connection)
        }

        private fun add(connection: Update) {
            if (updates.contains(connection)) {
                throw IllegalStateException("duplicate stream with key: ${connection.keyAsString()}")
            }

            updates.add(connection)
        }

        private fun <StreamInput : Any, StreamOutput> createConnection(
            key: String = "",
            stream: Stream<StreamInput, StreamOutput>,
            input: StreamInput,
            onEvent: Transition.Factory.(StreamOutput) -> Transition<State, Output>
        ): Update.Stream<StreamInput, StreamOutput> {
            return Update.Stream(
                key = Update.Stream.Key(input, stream::class, key),
                input = input,
                stream = stream,
                onEvent = {
                    val value = onEvent(Transition.Factory, it)
                    transitionCallback(value)
                }
            )
        }
    }
}
