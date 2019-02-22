package com.instacart.client.mvi

import arrow.core.Option
import arrow.core.toOption
import com.instacart.formula.ICMviState

/**
 * Represents state of multiple mvi contracts.
 */
data class ICBasicMviFlowState<Key>(
    val activeKeys: ICActiveMviKeys<Key> = ICActiveMviKeys.empty(),
    val contracts: Map<Key, ICMviState<Key, *>> = emptyMap()
) {

    fun update(state: ICMviState<Key, *>): ICBasicMviFlowState<Key> {
        return copy(contracts = contracts.plus(state.key to state))
    }

    fun currentScreenState(): Option<ICMviState<Key, *>> {
        val currentKey = activeKeys.activeKeys.lastOrNull()
        return currentKey?.let { contracts[it] }.toOption()
    }
}
