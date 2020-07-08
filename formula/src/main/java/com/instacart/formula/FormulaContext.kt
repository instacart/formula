package com.instacart.formula

import com.instacart.formula.internal.JoinedKey
import com.instacart.formula.internal.ScopedCallbacks
import io.reactivex.rxjava3.core.Observable

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
     * @param key key with which the callback is to be associated. Same key cannot be used for multiple callbacks.
     */
    inline fun callback(
        key: Any,
        crossinline transition: Transition.Factory.() -> Transition<State>
    ): () -> Unit {
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
     * @param key key with which the callback is to be associated. Same key cannot be used for multiple callbacks.
     */
    inline fun <UIEvent> eventCallback(
        key: Any,
        crossinline transition: Transition.Factory.(UIEvent) -> Transition<State>
    ): (UIEvent) -> Unit {
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
    fun <ChildInput, ChildRenderModel> child(
        formula: IFormula<ChildInput, ChildRenderModel>
    ): Child<ChildInput, ChildRenderModel> {
        return child("", formula)
    }

    /**
     * Starts building a child [Formula]. The state management of child [Formula]
     * will be managed by the runtime. Call [Child.input] to finish declaring the child
     * and receive the [ChildRenderModel].
     *
     * @param key A unique identifier for this formula.
     */
    abstract fun <ChildInput, ChildRenderModel> child(
        key: Any,
        formula: IFormula<ChildInput, ChildRenderModel>
    ): Child<ChildInput, ChildRenderModel>

    /**
     * Provides an [UpdateBuilder] that enables [Formula] to declare various events and effects.
     */
    abstract fun updates(init: UpdateBuilder<State>.() -> Unit): List<Update<*>>

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
        internal val updates = mutableListOf<Update<*>>()

        /**
         * Adds a [Stream] as part of this [Evaluation]. [Stream] will be subscribed when it is initially added
         * and unsubscribed when it is not returned as part of [Evaluation].
         *
         * @param transition Callback invoked when [Stream] sends us a [Message].
         */
        inline fun <Message> events(
            stream: Stream<Message>,
            crossinline transition: Transition.Factory.(Message) -> Transition<State>
        ) {
            add(createConnection(stream, transition))
        }

        /**
         * Adds an [Observable] as part of this [Evaluation]. Observable will be subscribed when it is initially added
         * and unsubscribed when it is not returned as part of [Evaluation].
         */
        inline fun <Message> events(
            observable: Observable<Message>,
            crossinline transition: Transition.Factory.(Message) -> Transition<State>
        ) {
            events(RxStream.fromObservable { observable }, transition)
        }

        @PublishedApi internal fun add(connection: Update<*>) {
            if (updates.contains(connection)) {
                throw IllegalStateException("duplicate stream with key: ${connection.keyAsString()}")
            }

            updates.add(connection)
        }

        @PublishedApi internal inline fun <Message> createConnection(
            stream: Stream<Message>,
            crossinline transition: Transition.Factory.(Message) -> Transition<State>
        ): Update<Message> {
            val callback: (Message) -> Unit = {
                val value = transition(Transition.Factory, it)
                transitionCallback(value)
            }

            return Update(
                key = JoinedKey(stream.key(), callback::class),
                stream = stream,
                initial = callback
            )
        }
    }
}
