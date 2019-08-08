package com.instacart.formula

import kotlin.reflect.KClass

class Update<Input : Any, Output>(
    val key: Key,
    val input: Input,
    val stream: Stream<Input, Output>,
    onEvent: (Output) -> Unit
) {
    /**
     * A way to ensure uniqueness and equality between [Update]s.
     */
    data class Key(
        val input: Any,
        val type: KClass<*>,
        val extra: Any? = null
    )

    internal var handler: (Output) -> Unit = onEvent
    internal var cancelation: Cancelation? = null

    internal fun start() {
        cancelation = stream.perform(input) { next ->
            handler.invoke(next)
        }
    }

    internal fun tearDown() {
        cancelation?.cancel()
        cancelation = null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Update<*, *>

        if (key != other.key) return false

        return true
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }

    fun keyAsString(): String {
        return key.toString()
    }
}
