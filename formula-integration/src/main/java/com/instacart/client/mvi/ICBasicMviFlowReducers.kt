package com.instacart.client.mvi

import com.instacart.formula.ICMviState

class ICBasicMviFlowReducers<Key : Any>(
    private val root: ICMviBinding.CompositeBinding<Unit, Key, Unit>
) {

    fun onBackstackChange(keys: ICActiveMviKeys<Key>): (ICBasicMviFlowState<Key>) -> ICBasicMviFlowState<Key> {
        return { state: ICBasicMviFlowState<Key> ->
            val attachedKeys = ICActiveMviKeys.findAttachedKeys(
                lastActive = state.activeKeys.activeKeys,
                currentlyActive = keys.activeKeys
            )

            val detached = ICActiveMviKeys.findDetachedKeys(
                lastActive = state.activeKeys.activeKeys,
                currentlyActive = keys.activeKeys
            )

            // We want to emit an empty state update if key is not handled.
            val notHandled = attachedKeys
                .filter { !root.binds(it) }
                .map { Pair(it, ICMviState(it, "missing-registration")) }

            state.copy(
                activeKeys = keys,
                contracts = state.contracts.minus(detached).plus(notHandled)
            )
        }
    }

    fun onScreenStateChanged(event: ICMviState<Key, Any>): (ICBasicMviFlowState<Key>) -> ICBasicMviFlowState<Key> {
        return { state: ICBasicMviFlowState<Key> ->
            state.update(event)
        }
    }
}
