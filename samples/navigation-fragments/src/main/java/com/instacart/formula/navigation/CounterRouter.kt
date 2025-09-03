package com.instacart.formula.navigation

import com.instacart.formula.android.ActivityStoreContext

interface CounterRouter {
    fun onNavigateBack()
    fun onNavigateToNext(nextCounterIndex: Int)
}

class CounterRouterImpl(
    private val store: ActivityStoreContext<NavigationActivity>,
) : CounterRouter {
    override fun onNavigateBack() {
        store.send { onBackPressed() }
    }

    override fun onNavigateToNext(nextCounterIndex: Int) {
        store.send { showNextFragment(nextCounterIndex) }
    }
}