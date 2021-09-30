package com.instacart.formula.internal

import com.instacart.formula.Transition
import com.instacart.formula.Update

@PublishedApi
internal class TransitionDispatcher<State>(
    private val handleTransition: (Transition<State>) -> Unit,
    var transitionId: TransitionId
) {
    var running = false
    var terminated = false

    fun dispatch(transition: Transition<State>) {
        if (!running) {
            throw IllegalStateException("Transitions are not allowed during evaluation")
        }

        if (TransitionUtils.isEmpty(transition)) {
            return
        }

        if (!terminated && transitionId.hasTransitioned()) {
            // We have already transitioned, this should not happen.
            throw IllegalStateException("Transition already happened. This is using old transition callback: $transition.")
        }

        handleTransition(transition)
    }

    // TODO: naming UpdateType as transition seems weird. We need to resolve these two types.
    fun <Event> dispatch(transition: Update<State, Event>, event: Event) {
        val transition = Transition.Factory.run { transition.run { create(event) } }
        dispatch(transition)
    }

    /**
     * We define this as inline function because we want to generate an anonymous class at the
     * call-site because we will use the type as key.
     */
    fun <Event> toCallback(
        update: Update<State, Event>
    ): (Event) -> Unit {
        return {
            val transition = Transition.Factory.run { update.run { create(it) } }
            dispatch(transition)
        }
    }
}
