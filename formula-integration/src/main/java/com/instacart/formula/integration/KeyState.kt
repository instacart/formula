package com.instacart.formula.integration

/**
 * Describes the state of live contract
 */
data class KeyState<Key, RenderModel>(val key: Key, val renderModel: RenderModel)
