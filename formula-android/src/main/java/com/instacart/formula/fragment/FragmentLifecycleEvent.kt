package com.instacart.formula.fragment

import com.instacart.formula.integration.ActiveFragment

/**
 * Models when a fragment key is attached and detached. Provides a way to indicate
 * when to initialize state stream and when to destroy it.
 */
sealed class FragmentLifecycleEvent() {

    abstract val fragmentId: String
    abstract val key: FragmentKey

    fun activeFragment() = ActiveFragment(fragmentId, key)

    data class Active(
        override val fragmentId: String,
        override val key: FragmentKey
    ) : FragmentLifecycleEvent()

    data class Removed(
        override val fragmentId: String,
        override val key: FragmentKey,
        val lastState: Any? = null
    ) : FragmentLifecycleEvent()
}
