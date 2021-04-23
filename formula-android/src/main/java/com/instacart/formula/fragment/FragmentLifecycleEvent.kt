package com.instacart.formula.fragment

/**
 * Models when a fragment key is attached and detached. Provides a way to indicate
 * when to initialize state stream and when to destroy it.
 */
sealed class FragmentLifecycleEvent(val key: FragmentKey) {
    class Added(key: FragmentKey) : FragmentLifecycleEvent(key)
    class Removed(key: FragmentKey, val lastState: Any? = null) : FragmentLifecycleEvent(key)
}
