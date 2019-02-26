package com.instacart.formula.integration

/**
 * Models when a key is attached and detached. Provides a way to indicate
 * when to initialize state stream and when to destroy it.
 */
sealed class LifecycleEvent<Key>(val key: Key) {
    class Added<Key>(key: Key) : LifecycleEvent<Key>(key)
    class Removed<Key>(key: Key, val lastState: Any? = null) : LifecycleEvent<Key>(key)
}
