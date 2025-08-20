package com.instacart.formula.navigation

sealed class NavigationAction {
    data class NavigateToFragment(val fragmentId: Int) : NavigationAction()
    data object NavigateBack : NavigationAction()
}