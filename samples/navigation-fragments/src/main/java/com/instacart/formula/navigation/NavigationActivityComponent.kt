package com.instacart.formula.navigation

import com.instacart.formula.android.ActivityStoreContext
import com.instacart.formula.android.FragmentState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.Flow

class NavigationActivityComponent(
    private val store: ActivityStoreContext<NavigationActivity>,
) : CounterFragmentFormula.Dependencies {

    override val navigationStack: SharedFlow<List<Int>>
        get() = requireNavigationOutput().navigationStack

    override val counterIncrements: SharedFlow<Int>
        get() = requireNavigationOutput().counterIncrements

    override val onNavigateToNext: () -> Unit
        get() = requireNavigationOutput().onNavigateToNext

    override val onNavigateBack: () -> Unit
        get() = requireNavigationOutput().onNavigateBack

    override val onIncrementCounter: (Int) -> Unit
        get() = requireNavigationOutput().onIncrementCounter

    fun fragmentState(): Flow<FragmentState> {
        return store.fragmentState()
    }

    private fun requireNavigationOutput(): NavigationActivityFormula.Output {
        var navigationOutput: NavigationActivityFormula.Output? = null
        store.send { navigationOutput = getNavigationOutput() }
        return checkNotNull(navigationOutput) { "Navigation output not available" }
    }
}