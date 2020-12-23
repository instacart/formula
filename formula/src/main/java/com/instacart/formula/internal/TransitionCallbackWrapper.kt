package com.instacart.formula.internal

import com.instacart.formula.Transition

internal class TransitionCallbackWrapper<State>(
    private val handleTransition: (Transition<State>) -> Unit,
    var transitionId: TransitionId
) : (Transition<State>) -> Unit {
    var running = false
    var terminated = false

    override fun invoke(transition: Transition<State>) {
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
