package com.instacart.formula.navigation

import com.instacart.formula.android.ActivityStoreContext
import com.instacart.formula.android.FragmentState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.Flow

class NavigationActivityComponent(
    private val store: ActivityStoreContext<NavigationActivity>,
) : CounterFragmentFeatureFactory.Dependencies {

    override fun navigationStack(): SharedFlow<List<Int>> {
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

    fun fragmentState(): Flow<FragmentState> {
        return store.fragmentState()
    }

    private fun requireNavigationOutput(): NavigationActivityFormula.Output {
        var navigationOutput: NavigationActivityFormula.Output? = null
        store.send { navigationOutput = getNavigationOutput() }
        return checkNotNull(navigationOutput) { "Navigation output not available" }
    }
}