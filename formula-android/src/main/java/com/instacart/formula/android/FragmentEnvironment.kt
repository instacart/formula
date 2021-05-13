package com.instacart.formula.android

data class FragmentEnvironment(
    val onScreenError: (FragmentKey, Throwable) -> Unit = { _, it -> throw it }
)
