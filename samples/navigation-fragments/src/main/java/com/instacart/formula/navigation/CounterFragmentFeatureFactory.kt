package com.instacart.formula.navigation

import com.instacart.formula.android.Feature
import com.instacart.formula.android.FeatureFactory
import com.instacart.formula.runAsStateFlow

class CounterFragmentFeatureFactory : FeatureFactory<CounterFragmentFormula.Dependencies, CounterFragmentKey>() {

    override fun Params.initialize(): Feature {
        return Feature(CounterFragmentViewFactory()) {
            val formula = CounterFragmentFormula(dependencies)
            formula.runAsStateFlow(
                scope = it,
                input = CounterFragmentFormula.Input(
                    fragmentId = key.fragmentId,
                ),
            )
        }
    }
}