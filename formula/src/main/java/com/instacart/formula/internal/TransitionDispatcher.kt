package com.instacart.formula.internal

import com.instacart.formula.Transition

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
}
