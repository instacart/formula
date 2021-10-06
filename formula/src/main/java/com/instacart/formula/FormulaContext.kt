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
        return onEvent(Unit, transition)
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
        val type = transition::class
        val joinedKey: Any = if (key == Unit) {
            type
        } else {
            JoinedKey(type, key)
        }
        val listener = listeners.initOrFindListener<Event>(joinedKey)
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
        transition: Transition.Factory.(Unit) -> Transition<State>
    ): () -> Unit {
        val listener = onEvent(key, transition)
        return UnitListener(listener)
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
    abstract fun updates(init: StreamBuilder<State>.() -> Unit): List<BoundStream<*>>

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

}