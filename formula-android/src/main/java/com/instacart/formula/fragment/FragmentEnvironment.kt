package com.instacart.formula.fragment

import com.instacart.formula.android.FragmentKey

data class FragmentEnvironment(
    val onScreenError: (FragmentKey, Throwable) -> Unit = { _, it -> throw it }
)
