package com.instacart.formula.internal

import com.instacart.formula.Transition
import kotlin.reflect.KClass

internal fun interface TransitionListener {
    fun onTransitionResult(
        formulaType: KClass<*>,
        result: Transition.Result<*>,
        isValid: Boolean,
    )
}