package com.instacart.client.mvi

class DisposableScope<out Component>(
    val component: Component,
    private val onDispose: () -> Unit
) {

    fun dispose() = onDispose()
}
