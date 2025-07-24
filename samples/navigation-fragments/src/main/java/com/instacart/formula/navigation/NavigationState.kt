package com.instacart.formula.navigation

data class NavigationState(
    val navigationStack: List<Int> = listOf(0), // Start with fragment 0
) {
    fun navigateToFragment(fragmentId: Int): NavigationState = copy(
        navigationStack = navigationStack + fragmentId,
    )

    fun navigateBack(): NavigationState = copy(
        navigationStack = if (navigationStack.size > 1) {
            navigationStack.dropLast(1)
        } else {
            navigationStack
        },
    )

    val currentFragmentId: Int get() = navigationStack.lastOrNull() ?: 0
}