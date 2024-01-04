package com.instacart.formula

import com.instacart.formula.internal.Listeners
import com.instacart.formula.internal.UnitListener
import kotlin.reflect.KClass

/**
 * Provides functionality within [evaluate][Formula.evaluate] function to [compose][child]
 * child formulas, handle events [FormulaContext.onEvent], and [respond][FormulaContext.actions]
 * to arbitrary asynchronous events.
 */
abstract class FormulaContext<out Input, State> internal constructor(
    @PublishedApi internal val listeners: Listeners,
) {

    /**
     * Creates a listener that takes an event and performs a [Transition]. It uses a composite
     * key of [transition] type and optional [key] property.
     *
     * @param key Optional key property that the listener will be associated with. Same key value
     * should not be used with the same [transition] type.
     */
    fun callback(
        key: Any? = null,
        transition: Transition<Input, State, Unit>,
    ): () -> Unit {
        val listener = onEvent(key, transition)
        return UnitListener(listener)
    }

    /**
     * Creates a [Listener] that takes a [Event] and performs a [Transition]. It uses a composite
     * key of [transition] type and optional [key] property.
     *
     * @param key Optional key property that the listener will be associated with. Same key value
     * should not be used with the same [transition] type.
     */
    fun <Event> onEvent(
        key: Any? = null,
        transition: Transition<Input, State, Event>,
    ): Listener<Event> {
        return eventListener(
            key = createScopedKey(transition.type(), key),
            transition = transition
        )
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
     * Builds a list of deferred actions that will be executed by Formula runtime.
     */
    abstract fun actions(init: ActionBuilder<Input, State>.() -> Unit): Set<DeferredAction<*>>

    /**
     * Scopes [create] block with a [key].
     *
     * @param key Unique identifier that will be used for this block.
     */
    inline fun <Value> key(key: Any, create: () -> Value): Value {
        enterScope(key)
        val value = create()
        endScope()
        return value
    }

    // Internal listener management
    internal abstract fun <Event> eventListener(
        key: Any,
        useIndex: Boolean = true,
        transition: Transition<Input, State, Event>
    ): Listener<Event>

    // Internal key scope management
    @PublishedApi internal abstract fun enterScope(key: Any)
    @PublishedApi internal abstract fun endScope()
    @PublishedApi internal abstract fun createScopedKey(type: KClass<*>, key: Any? = null): Any
}