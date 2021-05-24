package com.instacart.formula.android

data class FragmentEnvironment(
    val logger: (String) -> Unit = {},
    val onScreenError: (FragmentKey, Throwable) -> Unit = { _, it -> throw it },
)
