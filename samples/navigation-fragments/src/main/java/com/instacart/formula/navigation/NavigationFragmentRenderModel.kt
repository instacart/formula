package com.instacart.formula.navigation

data class NavigationFragmentRenderModel(
    val fragmentId: Int,
    val counter: Int,
    val backStackFragments: List<Int>,
    val onNavigateToNext: () -> Unit,
    val onNavigateBack: () -> Unit,
    val onIncrementCounter: (Int) -> Unit,
    val onIncrementLocalCounter: () -> Unit
)