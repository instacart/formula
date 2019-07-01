package com.instacart.formula.internal

/**
 * Provides ability if there was a transition.
 */
interface TransitionLock {

    /**
     * Used within [nextFrame] to indicate if the [nextFrame] has triggered a transition change.
     * Transition change means that the state has changed so we need to short circuit and do
     * another processing round.
     */
    fun hasTransitioned(transitionNumber: Long): Boolean
}
