package com.instacart.formula.navigation

sealed class NavigationEffect {
    data class NavigateToFragment(val fragmentId: Int) : NavigationEffect()
    data object NavigateBack : NavigationEffect()
}