package com.instacart.formula.internal

import com.instacart.formula.Effects
import kotlin.reflect.KClass

interface ManagerDelegate {

    /**
     * Called when there is a change within a formula such as re-evaluation and/or
     * effects that need to be executed.
     */
    fun onUpdate(formulaType: KClass<*>, effects: Effects?, evaluate: Boolean)
}