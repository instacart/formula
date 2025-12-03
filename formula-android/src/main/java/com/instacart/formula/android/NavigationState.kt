package com.instacart.formula.android

/**
 * Represents currently [activeIds] and their [outputs].
 *
 * @param activeIds route ids that are running their state management.
 * @param visibleIds route ids that are currently visible to the user.
 * @param outputs Last emitted output of each active [RouteId].
 */
data class NavigationState(
    val activeIds: List<RouteId<*>> = emptyList(),
    val visibleIds: List<RouteId<*>> = emptyList(),
    val outputs: Map<RouteId<*>, RouteOutput> = emptyMap(),
    internal val features: Map<RouteId<*>, FeatureEvent> = emptyMap(),
) {
    fun visibleOutput() = visibleIds.lastOrNull()?.let { outputs[it] }
}

@Deprecated(
    message = "FragmentState has been renamed to NavigationState",
    replaceWith = ReplaceWith("NavigationState", "com.instacart.formula.android.NavigationState")
)
typealias FragmentState = NavigationState
