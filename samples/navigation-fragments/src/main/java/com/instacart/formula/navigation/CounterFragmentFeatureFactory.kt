package com.instacart.formula.navigation

import com.instacart.formula.android.Feature
import com.instacart.formula.android.FeatureFactory
import com.instacart.formula.runAsStateFlow

class CounterFragmentFeatureFactory(
    private val navigationStore: NavigationStore,
    private val onNavigationEffect: (NavigationEffect) -> Unit,
) : FeatureFactory<Unit, CounterFragmentKey>() {

    override fun Params.initialize(): Feature {
        return Feature(CounterFragmentViewFactory()) {
            val formula = CounterFragmentFormula(navigationStore)
            formula.runAsStateFlow(
                scope = it,
                input = CounterFragmentFormula.Input(
                    fragmentId = key.fragmentId,
                    onNavigationEffect = onNavigationEffect,
                ),
            )
        }
    }
}