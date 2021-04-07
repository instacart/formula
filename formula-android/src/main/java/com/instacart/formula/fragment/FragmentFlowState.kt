package com.instacart.formula.fragment

import com.instacart.formula.android.Feature
import com.instacart.formula.integration.KeyState

/**
 * Represents currently [activeKeys] and their [states].
 *
 * @param activeKeys Fragment contracts that are running their state management.
 * @param visibleKeys Fragment contracts that are currently visible to the user.
 * @param states Last emitted state of each active [FragmentContract].
 */
data class FragmentFlowState(
    val activeKeys: List<FragmentKey> = emptyList(),
    val visibleKeys: List<FragmentKey> = emptyList(),
    val features: Map<FragmentKey, Feature<*>> = emptyMap(),
    val states: Map<FragmentKey, KeyState> = emptyMap()
) {
    fun visibleState() = visibleKeys.lastOrNull()?.let { states[it] }
}
