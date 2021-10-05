package com.instacart.formula.internal

import com.instacart.formula.Transition

internal fun interface TransitionListener {
    fun onTransition(transition: Transition<*>, isValid: Boolean)
}