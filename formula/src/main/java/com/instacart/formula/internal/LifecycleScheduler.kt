package com.instacart.formula.internal

internal interface LifecycleScheduler {
    fun scheduleTerminateEffect(effect: () -> Unit)
}
