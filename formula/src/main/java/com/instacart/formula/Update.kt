package com.instacart.formula

import kotlin.reflect.KClass

class Update<Data : Any, Message>(
    val key: Any,
    val data: Data,
    val stream: Stream<Data, Message>,
    initial: (Message) -> Unit
) {
    /**
     * A way to ensure uniqueness and equality between [Update]s.
     */
    data class Key(
        val data: Any,
        val type: KClass<*>,
        val extra: Any? = null
    )

    internal var handler: (Message) -> Unit = initial
    internal var cancelable: Cancelable? = null

    internal fun start() {
        cancelable = stream.start(data) { message ->
            handler.invoke(message)
        }
    }

    internal fun tearDown() {
        cancelable?.cancel()
        cancelable = null
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
