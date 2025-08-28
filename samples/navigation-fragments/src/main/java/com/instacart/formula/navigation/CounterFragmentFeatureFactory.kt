package com.instacart.formula.navigation

import com.instacart.formula.android.Feature
import com.instacart.formula.android.FeatureFactory
import com.instacart.formula.runAsStateFlow
import kotlinx.coroutines.flow.SharedFlow

class CounterFragmentFeatureFactory : FeatureFactory<CounterFragmentFeatureFactory.Dependencies, CounterFragmentKey>() {

    interface Dependencies {
        fun navigationStack(): SharedFlow<List<Int>>
        fun counterIncrements(): SharedFlow<Int>
        fun onNavigateToNext(): () -> Unit
        fun onNavigateBack(): () -> Unit
        fun onIncrementCounter(): (Int) -> Unit
    }

    override fun Params.initialize(): Feature {
        return Feature(CounterFragmentViewFactory()) {
            val formula = CounterFragmentFormula()
            formula.runAsStateFlow(
                scope = it,
                input = CounterFragmentFormula.Input(
                    fragmentId = key.fragmentId,
                    navigationStackFlow = dependencies.navigationStack(),
                    counterIncrements = dependencies.counterIncrements(),
                    onNavigateToNext = dependencies.onNavigateToNext(),
                    onNavigateBack = dependencies.onNavigateBack(),
                    onIncrementCounter = dependencies.onIncrementCounter(),
                ),
            )
        }
    }
}