package com.instacart.formula.integration

import arrow.core.Option
import arrow.core.toOption

/**
 * Representation of the [BackStack] and the state associated with each of the entries.
 *
 * [Key] - type representing the entry in the [BackStack]
 */
data class FlowState<Key>(
    val backStack: BackStack<Key> = BackStack.empty(),
    val contracts: Map<Key, KeyState<Key, *>> = emptyMap()
) {

    fun update(state: KeyState<Key, *>): FlowState<Key> {
        return copy(contracts = contracts.plus(state.key to state))
    }

    fun lastEntry(): KeyState<Key, *>? {
        val currentKey = backStack.keys.lastOrNull()
        return currentKey?.let { contracts[it] }
    }

    fun currentScreenState(): Option<KeyState<Key, *>> {
        val currentKey = backStack.keys.lastOrNull()
        return currentKey?.let { contracts[it] }.toOption()
    }
}
