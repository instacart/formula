package com.instacart.formula.navigation

data class CounterFragmentRenderModel(
    val fragmentId: Int,
    val counter: Int,
    val backStackFragments: List<Int>,
    val onNavigateToNext: () -> Unit,
    val onNavigateBack: () -> Unit,
    val onIncrementCounter: (Int) -> Unit,
)