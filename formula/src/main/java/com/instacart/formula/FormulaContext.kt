package com.instacart.formula

import com.instacart.formula.internal.Listeners
import com.instacart.formula.internal.TransitionDispatcher
import com.instacart.formula.internal.UnitListener
import kotlin.reflect.KClass

/**
 * Provides functionality within [evaluate][Formula.evaluate] function to [compose][child]
 * child formulas, handle events [FormulaContext.onEvent], and [respond][FormulaContext.actions]
 * to arbitrary asynchronous events.
 */
abstract class FormulaContext<out Input, State> internal constructor(
    @PublishedApi internal val listeners: Listeners,
    internal val transitionDispatcher: TransitionDispatcher<Input, State>,
) {

    /**
     * Creates a [Listener] that takes an [Event] and performs a [Transition].
     *
     * It uses [transition] type as key.
     */
    fun <Event> onEvent(
        transition: Transition<Input, State, Event>,
    ): Listener<Event> {
        return eventListener(
            key = createScopedKey(transition.type()),
            transition = transition
        )
    }

    /**
     * Creates a [Listener] that takes a [Event] and performs a [Transition].
     *
     * @param key key with which the listener is to be associated. Same key cannot be used for multiple listeners.
     */
    fun <Event> onEvent(
        key: Any,
        transition: Transition<Input, State, Event>,
    ): Listener<Event> {
        return eventListener(
            key = createScopedKey(transition.type(), key),
            transition = transition
        )
    }

    /**
     * Creates a listener that takes an event and performs a [Transition].
     *
     * It uses [transition] type as key.
     */
    fun callback(transition: Transition<Input, State, Unit>): () -> Unit {
        val listener = onEvent(transition)
        return UnitListener(listener)
    }

    /**
     * Creates a listener that takes an event and performs a [Transition].
     *
     * @param key key with which the listener is to be associated. Same key cannot be used for multiple listeners.
     */
    fun callback(
        key: Any,
        transition: Transition<Input, State, Unit>,
    ): () -> Unit {
        val listener = onEvent(key, transition)
        return UnitListener(listener)
    }

    /**
     * Creates a listener that takes a [Event] and performs a [Transition].
     *
     * It uses [transition] type as key.
     */
    @Deprecated("Use context.onEvent {} instead.", replaceWith = ReplaceWith("onEvent(transition)"))
    fun <Event> eventCallback(
        transition: Transition<Input, State, Event>,
    ): Listener<Event> {
        return onEvent(transition)
    }

    /**
     * Creates a listener that takes a [Event] and performs a [Transition].
     *
     * @param key key with which the listener is to be associated. Same key cannot be used for multiple listeners.
     */
    @Deprecated("Use context.onEvent(key) {} instead.", replaceWith = ReplaceWith("onEvent(key, transition)"))
    fun <Event> eventCallback(
        key: Any,
        transition: Transition<Input, State, Event>,
    ): Listener<Event> {
        return onEvent(key, transition)
    }

    /**
     * A convenience method to run a formula that takes no input. Returns the latest output
     * of the [formula] formula. Formula runtime ensures the [formula] is running, manages
     * its internal state and will trigger `evaluate` if needed.
     */
    fun <ChildOutput> child(
        formula: IFormula<Unit, ChildOutput>
    ): ChildOutput {
        return child(formula, Unit)
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
     * Provides an [ActionBuilder] that enables [Formula] to declare various events and effects.
     */
    @Deprecated("Stream was renamed to Action, context.actions replaces action.updates")
    abstract fun updates(init: ActionBuilder<Input, State>.() -> Unit): List<DeferredAction<*>>

    /**
     * Builds a list of deferred actions that will be executed by Formula runtime.
     */
    abstract fun actions(init: ActionBuilder<Input, State>.() -> Unit): List<DeferredAction<*>>

    /**
     * Scopes [create] block with a [key].
     *
     * @param key Unique identifier that will be used for this block.
     */
    inline fun <Value> key(key: Any, create: () -> Value): Value {
        ensureNotRunning()

        enterScope(key)
        val value = create()
        endScope()
        return value
    }

    internal fun <Event> eventListener(
        key: Any,
        transition: Transition<Input, State, Event>
    ): Listener<Event> {
        ensureNotRunning()
        val listener = listeners.initOrFindListener<Input, State, Event>(key)
        listener.transitionDispatcher = transitionDispatcher
        listener.transition = transition
        return listener
    }

    // Internal key scope management
    @PublishedApi internal abstract fun enterScope(key: Any)
    @PublishedApi internal abstract fun endScope()
    @PublishedApi internal abstract fun createScopedKey(type: KClass<*>, key: Any? = null): Any

    // Internal validation
    @PublishedApi internal abstract fun ensureNotRunning()
}