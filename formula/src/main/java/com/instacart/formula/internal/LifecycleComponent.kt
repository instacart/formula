package com.instacart.formula.internal

internal interface LifecycleComponent {
    fun onDetached(scheduler: LifecycleScheduler) = Unit
    fun onDuplicateKey(log: DuplicateKeyLog, key: Any) = Unit
}
