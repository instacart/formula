package com.instacart.formula.navigation

sealed class NavigationEffect {
    data class NavigateToFragment(val fragmentId: Int) : NavigationEffect()
    object NavigateBack : NavigationEffect()
}