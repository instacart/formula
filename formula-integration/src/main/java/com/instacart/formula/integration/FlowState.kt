package com.instacart.formula.integration

import arrow.core.Option
import arrow.core.toOption

/**
 * Represents state of multiple mvi contracts.
 */
data class FlowState<Key>(
    val backStack: BackStack<Key> = BackStack.empty(),
    val contracts: Map<Key, KeyState<Key, *>> = emptyMap()
) {

    fun update(state: KeyState<Key, *>): FlowState<Key> {
        return copy(contracts = contracts.plus(state.key to state))
    }

    fun currentScreenState(): Option<KeyState<Key, *>> {
        val currentKey = backStack.keys.lastOrNull()
        return currentKey?.let { contracts[it] }.toOption()
    }
}
