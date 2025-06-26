package com.instacart.formula.android

/**
 * Represents currently [activeIds] and their [outputs].
 *
 * @param activeIds Fragment contracts that are running their state management.
 * @param visibleIds Fragment contracts that are currently visible to the user.
 * @param outputs Last emitted output of each active [FragmentKey].
 */
data class FragmentState(
    val activeIds: List<FragmentId> = emptyList(),
    val visibleIds: List<FragmentId> = emptyList(),
    val outputs: Map<FragmentId, FragmentOutput> = emptyMap(),
    internal val features: Map<FragmentId, FeatureEvent> = emptyMap(),
) {
    fun visibleOutput() = visibleIds.lastOrNull()?.let { outputs[it] }
}
