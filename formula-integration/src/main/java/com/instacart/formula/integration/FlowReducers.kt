package com.instacart.formula.integration

class FlowReducers<Key : Any>(
    private val root: KeyBinding.CompositeBinding<Unit, Key, Unit>
) {

    fun onBackstackChange(keys: ActiveKeys<Key>): (FlowState<Key>) -> FlowState<Key> {
        return { state: FlowState<Key> ->
            val attachedKeys = ActiveKeys.findAttachedKeys(
                lastActive = state.activeKeys.activeKeys,
                currentlyActive = keys.activeKeys
            )

            val detached = ActiveKeys.findDetachedKeys(
                lastActive = state.activeKeys.activeKeys,
                currentlyActive = keys.activeKeys
            )

            // We want to emit an empty state update if key is not handled.
            val notHandled = attachedKeys
                .filter { !root.binds(it) }
                .map { Pair(it, KeyState(it, "missing-registration")) }

            state.copy(
                activeKeys = keys,
                contracts = state.contracts.minus(detached).plus(notHandled)
            )
        }
    }

    fun onScreenStateChanged(event: KeyState<Key, Any>): (FlowState<Key>) -> FlowState<Key> {
        return { state: FlowState<Key> ->
            state.update(event)
        }
    }
}
