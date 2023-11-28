package com.instacart.formula.android

/**
 * Represents currently [activeIds] and their [states].
 *
 * @param activeIds Fragment contracts that are running their state management.
 * @param visibleIds Fragment contracts that are currently visible to the user.
 * @param states Last emitted state of each active [FragmentKey].
 */
data class FragmentFlowState(
    val activeIds: List<FragmentId> = emptyList(),
    val visibleIds: List<FragmentId> = emptyList(),
    val features: Map<FragmentId, FeatureEvent> = emptyMap(),
    val states: Map<FragmentId, FragmentState> = emptyMap()
) {
    fun visibleState() = visibleIds.lastOrNull()?.let { states[it] }
}
