package com.instacart.formula


/**
 * Describes the state of live contract
 */
data class ICMviState<Key, State>(val key: Key, val state: State)
