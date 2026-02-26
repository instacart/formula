package com.instacart.formula.remember

fun interface RememberFactory<T> : () -> RememberComponent<T> {
    fun create(): T

    override fun invoke(): RememberComponent<T> {
        val value = create()
        return RememberComponent(value)
    }

    /**
     * Remember factory type is used as part of the key to distinguish different computations.
     */
    fun type(): Class<*> {
        return this::class.java
    }
}