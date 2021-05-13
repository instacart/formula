package com.instacart.formula.android.events

import com.instacart.formula.android.FragmentId

/**
 * Models when a fragment key is attached and detached. Provides a way to indicate
 * when to initialize state stream and when to destroy it.
 */
sealed class FragmentLifecycleEvent() {

    abstract val fragmentId: FragmentId

    data class Added(
        override val fragmentId: FragmentId
    ) : FragmentLifecycleEvent()

    data class Removed(
        override val fragmentId: FragmentId,
        val lastState: Any? = null
    ) : FragmentLifecycleEvent()
}
