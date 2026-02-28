package com.instacart.formula.lifecycle

internal interface LifecycleCache {

    fun <T : LifecycleComponent> findOrInit(
        key: Any,
        useIndex: Boolean,
        factory: () -> T,
    ): T
}
