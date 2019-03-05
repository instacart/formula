package com.instacart.formula


/**
 * When using reduce pattern, next allows to update the state + emit effects. Typically, you will extend
 * [Reducers] and will not need to use this class directly.
 */
data class Next<M, E>(
    val state: M,
    val effects: Set<E>
)
