package com.instacart.formula.navigation

import com.instacart.formula.android.ActivityStoreContext
import com.instacart.formula.android.FragmentState

class NavigationActivityComponent(
    activityStoreContext: ActivityStoreContext<NavigationActivity>,
) : CounterFragmentFeatureFactory.Dependencies {

    override val counterStore: CounterStore = CounterStore()
    override val counterRouter: CounterRouterImpl = CounterRouterImpl(activityStoreContext)

    fun onFragmentStateChanged(state: FragmentState) {
        counterStore.updateCounterStack(state.navStack())
    }
}

private fun extractFragmentId(fragmentKey: Any?): Int {
    return when (fragmentKey) {
        is CounterFragmentKey -> fragmentKey.fragmentId
        else -> throw RuntimeException("Unexpected fragment key: $fragmentKey")
    }
}

private fun FragmentState.navStack(): List<Int> = activeIds.map { extractFragmentId(it.key) }