package com.instacart.formula.navigation

sealed class NavigationEvent {
    data class NavigateToFragment(val fragmentId: Int) : NavigationEvent()
    data object NavigateBack : NavigationEvent()
    data class IncrementCounter(val fragmentId: Int) : NavigationEvent()
}