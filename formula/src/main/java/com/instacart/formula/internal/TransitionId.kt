package com.instacart.formula.internal

interface TransitionId {
    /**
     * Used during execution phase to check if a transition with a state change has occurred.
     * State change means that we need to exit out of this execution phase and go back to
     * evaluation.
     */
    fun hasTransitioned(): Boolean
}
