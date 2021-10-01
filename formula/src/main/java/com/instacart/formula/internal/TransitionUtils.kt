package com.instacart.formula.internal

import com.instacart.formula.Transition

internal object TransitionUtils {

    fun isEmpty(result: Transition.Result<*>): Boolean {
        return result == Transition.Result.None
    }
}
