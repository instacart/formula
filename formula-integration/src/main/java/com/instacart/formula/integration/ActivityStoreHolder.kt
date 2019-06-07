package com.instacart.formula.integration

import androidx.fragment.app.FragmentActivity

/**
 * Holds an instance of [ActivityStore].
 */
internal class ActivityStoreHolder<A : FragmentActivity>(
    val effectHandler: ActivityProxy<A>,
    val store: ActivityStore<A>
) {
    val state = store.fragmentFlowStore.state().replay(1)
    val subscription = state.connect()
}
