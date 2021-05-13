package com.instacart.formula.android

import com.instacart.formula.fragment.FragmentKey

/**
 * Defines the current render model for a specific [key].
 */
data class FragmentState(val key: FragmentKey, val renderModel: Any)
