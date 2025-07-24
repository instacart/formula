package com.instacart.formula.navigation

data class NavigationState(
    val fragmentCounters: Map<Int, Int> = emptyMap(),
    val navigationStack: List<Int> = listOf(0) // Start with fragment 0
) {
    fun getCounter(fragmentId: Int): Int = fragmentCounters[fragmentId] ?: 0

    fun incrementCounter(fragmentId: Int): NavigationState = copy(
        fragmentCounters = fragmentCounters.toMutableMap().apply {
            put(fragmentId, getCounter(fragmentId) + 1)
        }
    )

    fun navigateToFragment(fragmentId: Int): NavigationState = copy(
        navigationStack = navigationStack + fragmentId
    )

    fun navigateBack(): NavigationState = copy(
        navigationStack = if (navigationStack.size > 1) {
            navigationStack.dropLast(1)
        } else {
            navigationStack
        }
    )

    val currentFragmentId: Int get() = navigationStack.lastOrNull() ?: 0

    val backStackFragments: List<Int> get() = navigationStack.dropLast(1)
}