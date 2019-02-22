package com.instacart.client.mvi.state


/**
 * When using reduce pattern, next allows to update the state + emit effects
 */
data class ICNext<M, E>(
    val state: M,
    val effects: Set<E>
)
