package com.instacart.formula.integration

/**
 * Defines the current render model state for a specific [key].
 */
data class KeyState<Key, RenderModel>(val key: Key, val renderModel: RenderModel)
