package com.instacart.formula.internal

/**
 * Provides ability if there was a transition.
 */
interface TransitionLock {

    /**
     * Used to indicate if we the [nextFrame] has triggered a transition change. We want
     */
    fun hasTransitioned(transitionNumber: Long): Boolean
}
