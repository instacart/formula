package com.instacart.formula.internal

import com.instacart.formula.Transition

object TransitionUtils {

    fun isEmpty(transition: Transition<*, *>): Boolean {
        return transition.state == null
            && transition.output == null
            && transition.sideEffects.isEmpty()
    }
}