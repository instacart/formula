package com.instacart.formula.integration

import com.instacart.formula.fragment.FragmentKey

/**
 * Defines the current render model for a specific [key].
 */
data class KeyState(val key: FragmentKey, val renderModel: Any)
