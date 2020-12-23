package com.instacart.formula.internal

import com.instacart.formula.Transition

interface TransitionListener {
    companion object {
        inline operator fun invoke(crossinline listener: (Transition<*>, isValid: Boolean) -> Unit): TransitionListener {
            return object : TransitionListener {
                override fun onTransition(transition: Transition<*>, isValid: Boolean) {
                    listener(transition, isValid)
                }
            }
        }
    }

    fun onTransition(transition: Transition<*>, isValid: Boolean)
}