package com.instacart.formula.internal

import com.instacart.formula.Listener
import com.instacart.formula.Update

/**
 * Note: this class is not a data class because equality is based on instance and not [key].
 */
@PublishedApi
internal class CallbackImpl<State, Event>(internal var key: Any) : Listener<Event> {

    internal var transitionDispatcher: TransitionDispatcher<State>? = null
    internal var transition: Update<State, Event>? = null

    override fun invoke(event: Event) {
        transitionDispatcher?.let { dispatcher ->
            transition?.let { transition ->
                dispatcher.dispatch(transition, event)
            }
        }
        // TODO: log if null callback (it might be due to formula removal or due to callback removal)
    }

    fun disable() {
        transitionDispatcher = null
        transition = null
    }
}

/**
 * A wrapper to convert Listener<Unit> from (Unit) -> Unit into () -> Unit
 */
internal data class UnitCallback(val delegate: Listener<Unit>): () -> Unit {
    override fun invoke() = delegate(Unit)
}