package com.instacart.formula.internal

import com.instacart.formula.Effects

interface TransitionListener {
    companion object {
        inline operator fun invoke(crossinline listener: (Effects?, isValid: Boolean) -> Unit): TransitionListener {
            return object : TransitionListener {
                override fun onTransition(effects: Effects?, isValid: Boolean) {
                    listener(effects, isValid)
                }
            }
        }
    }

    fun onTransition(effects: Effects?, isValid: Boolean)
}