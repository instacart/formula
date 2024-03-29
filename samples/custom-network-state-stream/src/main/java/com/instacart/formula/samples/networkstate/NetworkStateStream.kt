package com.instacart.formula.samples.networkstate

import com.instacart.formula.Action

data class NetworkState(val isOnline: Boolean)

interface NetworkStateStream : Action<NetworkState> {
    /**
     * Using type as a key. There should not be more
     * than one subscription to this stream
     * within a formula instance.
     */
    override fun key(): Any = this::class.java
}
