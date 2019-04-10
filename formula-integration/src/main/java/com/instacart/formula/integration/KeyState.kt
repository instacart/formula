package com.instacart.formula.integration

/**
 * Defines the current render model state for a specific [key].
 */
data class KeyState<Key>(val key: Key, val renderModel: Any)
