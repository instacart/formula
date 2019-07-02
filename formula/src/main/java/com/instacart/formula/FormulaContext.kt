package com.instacart.formula

import io.reactivex.Observable

/**
 * This interface provides ability to [Formula] to trigger transitions, instantiate updates and create
 * child formulas.
 */
interface FormulaContext<State, Output> {

    /**
     * Creates a callback to be used for handling UI event transitions.
     */
    fun callback(wrap: Transition.Factory.() -> Transition<State, Output>): () -> Unit

    /**
     * Creates a callback that takes a [UIEvent] and performs a [Transition].
     */
    fun <UIEvent> eventCallback(wrap: Transition.Factory.(UIEvent) -> Transition<State, Output>): (UIEvent) -> Unit

    /**
     * Creates a callback to be used for handling UI event transitions.
     */
    fun callback(name: String, wrap: Transition.Factory.() -> Transition<State, Output>): () -> Unit

    /**
     * Creates a callback that takes a [UIEvent] and performs a [Transition].
     */
    fun <UIEvent> eventCallback(name: String, wrap: Transition.Factory.(UIEvent) -> Transition<State, Output>): (UIEvent) -> Unit

    /**
     * Declares a child [Formula] which returns the [ChildRenderModel]. The state management
     * of child Formula is managed by the runtime.
     */
    fun <ChildInput, ChildState, ChildOutput, ChildRenderModel> child(
        key: String,
        formula: Formula<ChildInput, ChildState, ChildOutput, ChildRenderModel>,
        input: ChildInput,
        onEvent: Transition.Factory.(ChildOutput) -> Transition<State, Output>
    ): ChildRenderModel

    /**
     * Declares a child [Formula] which returns the [ChildRenderModel]. The state management
     * of child Formula is managed by the runtime.
     */
    fun <ChildInput, ChildState, ChildOutput, ChildRenderModel> child(
        formula: Formula<ChildInput, ChildState, ChildOutput, ChildRenderModel>,
        input: ChildInput,
        onEvent: Transition.Factory.(ChildOutput) -> Transition<State, Output>
    ): ChildRenderModel {
        return child("", formula, input, onEvent)
    }

    /**
     * Declares a child [Formula] which returns the [ChildRenderModel]. The state management
     * of child Formula is managed by the runtime.
     */
    fun <ChildInput, ChildState, ChildRenderModel> child(
        formula: Formula<ChildInput, ChildState, Unit, ChildRenderModel>,
        input: ChildInput
    ): ChildRenderModel  {
        return child("", formula, input)
    }

    /**
     * Declares a child [Formula] that has no Output. The state management
     * of child Formula is managed by the runtime.
     */
    fun <ChildInput, ChildState, ChildRenderModel> child(
        key: String,
        formula: Formula<ChildInput, ChildState, Unit, ChildRenderModel>,
        input: ChildInput
    ): ChildRenderModel  {
        return child(key, formula, input, onEvent = {
            none()
        })
    }

    /**
     * Provides an [UpdateBuilder] that enables [Formula] to declare various events and effects.
     */
    fun updates(init: UpdateBuilder<State, Output>.() -> Unit): List<Update>

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
