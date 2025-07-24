package com.instacart.formula.navigation

import com.instacart.formula.android.Feature
import com.instacart.formula.android.FeatureFactory
import com.instacart.formula.rxjava3.toObservable

class NavigationFragmentFeatureFactory(
    private val navigationStore: NavigationStore,
    private val onNavigationEffect: (NavigationEffect) -> Unit,
) : FeatureFactory<Unit, NavigationFragmentKey>() {

    override fun Params.initialize(): Feature {
        val formula = NavigationFragmentFormula(navigationStore)
        return Feature(
            state = formula.toObservable(
                NavigationFragmentFormula.Input(
                    fragmentId = key.fragmentId,
                    onNavigationEffect = onNavigationEffect,
                )
            ),
            viewFactory = NavigationFragmentViewFactory(),
        )
    }
}