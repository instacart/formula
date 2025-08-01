package com.instacart.formula.navigation

import com.instacart.formula.android.ActivityStoreContext
import kotlinx.coroutines.flow.SharedFlow

class NavigationActivityComponent(
    private val store: ActivityStoreContext<NavigationActivity>,
) : CounterFragmentFeatureFactory.Dependencies {

    override fun navigationStack(): List<Int> {
        return requireNavigationOutput().navigationStack
    }

    override fun counterIncrements(): SharedFlow<Int> {
        return requireNavigationOutput().counterIncrements
    }

    override fun onNavigateToNext(): () -> Unit {
        return requireNavigationOutput().onNavigateToNext
    }

    override fun onNavigateBack(): () -> Unit {
        return requireNavigationOutput().onNavigateBack
    }

    override fun onIncrementCounter(): (Int) -> Unit {
        return requireNavigationOutput().onIncrementCounter
    }

    private fun requireNavigationOutput(): NavigationActivityFormula.Output {
        var navigationOutput: NavigationActivityFormula.Output? = null
        store.send { navigationOutput = getNavigationOutput() }
        return checkNotNull(navigationOutput) { "Navigation output not available" }
    }
}