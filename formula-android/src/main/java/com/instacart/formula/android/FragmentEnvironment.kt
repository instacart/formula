package com.instacart.formula.android

import com.instacart.formula.android.FragmentKey

data class FragmentEnvironment(
    val onScreenError: (FragmentKey, Throwable) -> Unit = { _, it -> throw it }
)
