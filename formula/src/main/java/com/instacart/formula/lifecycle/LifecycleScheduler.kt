package com.instacart.formula.lifecycle

internal interface LifecycleScheduler {
    fun scheduleStartEffect(effect: () -> Unit)
    fun scheduleTerminateEffect(effect: () -> Unit)
}
