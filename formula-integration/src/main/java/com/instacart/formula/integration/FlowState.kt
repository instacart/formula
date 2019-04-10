package com.instacart.formula.integration

/**
 * Representation of the [BackStack] and the state associated with each of the entries.
 *
 * @param Key type representing the entry in the [BackStack]
 */
data class FlowState<Key>(
    val backStack: BackStack<Key> = BackStack.empty(),
    val states: Map<Key, KeyState<Key>> = emptyMap()
) {

    fun update(state: KeyState<Key>): FlowState<Key> {
        return copy(states = states.plus(state.key to state))
    }

    fun lastEntry(): KeyState<Key>? {
        val currentKey = backStack.keys.lastOrNull()
        return currentKey?.let { states[it] }
    }
}
