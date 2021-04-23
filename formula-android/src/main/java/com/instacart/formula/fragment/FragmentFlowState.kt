package com.instacart.formula.fragment

import com.instacart.formula.integration.ActiveFragment
import com.instacart.formula.integration.FeatureEvent
import com.instacart.formula.integration.KeyState

/**
 * Represents currently [activeKeys] and their [states].
 *
 * @param activeKeys Fragment contracts that are running their state management.
 * @param visibleKeys Fragment contracts that are currently visible to the user.
 * @param states Last emitted state of each active [FragmentContract].
 */
data class FragmentFlowState(
    val activeKeys: List<ActiveFragment> = emptyList(),
    val visibleKeys: List<ActiveFragment> = emptyList(),
    val features: Map<ActiveFragment, FeatureEvent> = emptyMap(),
    val states: Map<ActiveFragment, KeyState> = emptyMap()
) {
    fun visibleState() = visibleKeys.lastOrNull()?.let { states[it] }
}
