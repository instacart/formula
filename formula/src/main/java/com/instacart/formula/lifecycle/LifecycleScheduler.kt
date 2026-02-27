package com.instacart.formula.lifecycle

interface LifecycleScheduler {
    fun scheduleTerminateEffect(effect: () -> Unit)
}
