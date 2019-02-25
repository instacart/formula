package com.instacart.client.mvi

/**
 * Models when a contract is attached and detached. Provides a way to indicate
 * when to initialize state stream and when to destroy it.
 */
sealed class ICMviLifecycleEvent<Key>(val key: Key) {
    class Attach<Key>(key: Key) : ICMviLifecycleEvent<Key>(key)
    class Detach<Key>(key: Key, val lastState: Any? = null) : ICMviLifecycleEvent<Key>(key)
}