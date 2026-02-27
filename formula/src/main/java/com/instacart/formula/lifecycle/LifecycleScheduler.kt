package com.instacart.formula.lifecycle

interface LifecycleScheduler {
    fun scheduleStartEffect(effect: () -> Unit)
    fun scheduleTerminateEffect(effect: () -> Unit)
}
