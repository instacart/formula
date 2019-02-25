package com.instacart.formula.integration

/**
 * Models when a contract is attached and detached. Provides a way to indicate
 * when to initialize state stream and when to destroy it.
 */
sealed class LifecycleEvent<Key>(val key: Key) {
    class Attach<Key>(key: Key) : LifecycleEvent<Key>(key)
    class Detach<Key>(key: Key, val lastState: Any? = null) : LifecycleEvent<Key>(key)
}
