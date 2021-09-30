package com.instacart.formula

class BoundStream<Message>(
    val key: Any,
    val stream: Stream<Message>,
    initial: (Message) -> Unit
) {

    internal var handler: (Message) -> Unit = initial
    internal var cancelable: Cancelable? = null

    internal fun start() {
        cancelable = stream.start() { message ->
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

        other as BoundStream<*>

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
