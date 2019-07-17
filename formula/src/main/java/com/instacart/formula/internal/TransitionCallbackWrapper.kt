package com.instacart.formula.internal

import com.instacart.formula.Transition

internal class TransitionCallbackWrapper<State, Output>(
    private val transitionLock: TransitionLock,
    private val handleTransition: (Transition<State, Output>, Boolean) -> Unit,
    var transitionNumber: Long
) : (Transition<State, Output>) -> Unit {
    var running = false


    override fun invoke(transition: Transition<State, Output>) {
        if (!running) {
            throw IllegalStateException("Transitions are not allowed during evaluation")
        }

        if (TransitionUtils.isEmpty(transition)) {
            return
        }

        if (transitionLock.hasTransitioned(transitionNumber)) {
            // We have already transitioned, this should not happen.
            throw IllegalStateException("Transition already happened. This is using old transition callback: $transition.")
        }

        handleTransition(transition, false)
    }
}
