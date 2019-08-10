package com.instacart.formula.internal

import com.instacart.formula.Transition

internal class TransitionCallbackWrapper<State>(
    private val transitionLock: TransitionLock,
    private val handleTransition: (Transition<State>, Boolean) -> Unit,
    var transitionId: Long
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

        if (!terminated && transitionLock.hasTransitioned(transitionId)) {
            // We have already transitioned, this should not happen.
            throw IllegalStateException("Transition already happened. This is using old transition callback: $transition.")
        }

        handleTransition(transition, false)
    }
}
