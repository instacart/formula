package com.instacart.formula.internal

import com.instacart.formula.Transition
import kotlin.reflect.KClass

interface ManagerDelegate {
    fun onTransition(
        formulaType: KClass<*>,
        result: Transition.Result<*>,
        evaluate: Boolean,
    )
}