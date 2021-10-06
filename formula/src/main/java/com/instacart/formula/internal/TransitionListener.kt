package com.instacart.formula.internal

import com.instacart.formula.Transition

internal fun interface TransitionListener {
    fun onTransitionResult(result: Transition.Result<*>, isValid: Boolean)
}