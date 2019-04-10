package com.instacart.formula.integration

class FlowReducers<Key : Any>(
    private val root: Binding<Unit, Key, Any>
) {

    fun onBackstackChange(keys: BackStack<Key>): (FlowState<Key>) -> FlowState<Key> {
        return { state: FlowState<Key> ->
            val attachedKeys = BackStack.findAttachedKeys(
                lastActive = state.backStack.keys,
                currentlyActive = keys.keys
            )

            val detached = BackStack.findDetachedKeys(
                lastActive = state.backStack.keys,
                currentlyActive = keys.keys
            )

            // We want to emit an empty state update if key is not handled.
            val notHandled = attachedKeys
                .filter { !root.binds(it) }
                .map { Pair(it, KeyState(it, "missing-registration")) }

            state.copy(
                backStack = keys,
                states = state.states.minus(detached).plus(notHandled)
            )
        }
    }

    fun onScreenStateChanged(event: KeyState<Key, Any>): (FlowState<Key>) -> FlowState<Key> {
        return { state: FlowState<Key> ->
            state.update(event)
        }
    }
}
