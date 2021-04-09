package com.instacart.formula.fragment

data class FragmentEnvironment(
    val onScreenError: (FragmentKey, Throwable) -> Unit = { _, it -> throw it }
)
