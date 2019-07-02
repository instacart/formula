package com.instacart.formula.internal

data class EventCallback<T>(val name: String): (T) -> Unit {
    internal lateinit var callback: (T) -> Unit

    override fun invoke(p1: T) {
        callback(p1)
    }
}
