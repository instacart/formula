package com.instacart.formula.integration


/**
 * Describes the state of live contract
 */
data class KeyState<Key, State>(val key: Key, val state: State)
