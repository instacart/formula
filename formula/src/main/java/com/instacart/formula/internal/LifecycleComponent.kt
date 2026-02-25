package com.instacart.formula.internal

internal interface LifecycleComponent {
    fun onRemove() = Unit
}