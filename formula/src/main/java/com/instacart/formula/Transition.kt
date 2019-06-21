package com.instacart.formula

data class Transition<out State, out Effect>(
    val state: State,
    val effect: Effect? = null
)
