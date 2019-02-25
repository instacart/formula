package com.instacart.formula


/**
 * When using reduce pattern, next allows to update the state + emit effects
 */
data class Next<M, E>(
    val state: M,
    val effects: Set<E>
)
