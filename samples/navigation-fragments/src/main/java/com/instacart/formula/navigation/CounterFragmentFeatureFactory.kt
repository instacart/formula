package com.instacart.formula.navigation

import com.instacart.formula.android.Feature
import com.instacart.formula.android.FeatureFactory
import com.instacart.formula.navigation.CounterFragmentFeatureFactory.Dependencies
import com.instacart.formula.runAsStateFlow

class CounterFragmentFeatureFactory : FeatureFactory<Dependencies, CounterRouteKey>() {

    interface Dependencies {
        val counterStore: CounterStore
        val counterRouter: CounterRouter
    }

    override fun Params.initialize(): Feature {
        return Feature(CounterFragmentViewFactory()) {
            val formula = CounterFragmentFormula(
                counterStore = dependencies.counterStore,
                counterRouter = dependencies.counterRouter,
            )
            formula.runAsStateFlow(
                scope = it,
                input = CounterFragmentFormula.Input(
                    counterIndex = key.fragmentId,
                ),
            )
        }
    }
}