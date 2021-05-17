package com.instacart.formula.android

/**
 * Represents currently [activeKeys] and their [states].
 *
 * @param activeKeys Fragment contracts that are running their state management.
 * @param visibleKeys Fragment contracts that are currently visible to the user.
 * @param states Last emitted state of each active [FragmentContract].
 */
data class FragmentFlowState(
    val activeIds: List<FragmentId> = emptyList(),
    val visibleIds: List<FragmentId> = emptyList(),
    val features: Map<FragmentId, FeatureEvent> = emptyMap(),
    val states: Map<FragmentId, FragmentState> = emptyMap()
) {
    @Deprecated("use activeIds")
    val activeKeys: List<FragmentKey>
        get() = activeIds.map { it.key }

    @Deprecated("use visibleIds")
    val visibleKeys: List<FragmentKey>
        get() = visibleIds.map { it.key }

    fun visibleState() = visibleIds.lastOrNull()?.let { states[it] }
}
