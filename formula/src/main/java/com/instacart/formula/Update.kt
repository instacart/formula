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
        val processorType: KClass<*>,
        val tag: String = ""
    )

    internal var handler: (Output) -> Unit = onEvent
    internal var disposable: Cancelation? = null

    internal fun start() {
        disposable = stream.perform(input) { next ->
            handler.invoke(next)
        }
    }

    internal fun tearDown() {
        disposable?.cancel()
        disposable = null
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
