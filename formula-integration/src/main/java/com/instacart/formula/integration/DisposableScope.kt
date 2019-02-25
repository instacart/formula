package com.instacart.formula.integration

class DisposableScope<out Component>(
    val component: Component,
    private val onDispose: () -> Unit
) {

    fun dispose() = onDispose()
}
