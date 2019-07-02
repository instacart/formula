package com.instacart.formula.internal

data class Callback(val name: String): () -> Unit {
    internal lateinit var callback: () -> Unit

    override fun invoke() {
        callback()
    }
}
