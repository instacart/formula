package com.instacart.formula.internal

import com.instacart.formula.Transition

internal object TransitionUtils {

    fun isEmpty(transition: Transition<*>): Boolean {
        return transition == Transition.None
    }
}
