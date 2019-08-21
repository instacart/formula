package com.instacart.formula

/**
 * An executable message.
 */
sealed class Message {
    internal abstract fun deliver()
}

data class UnitMessage(val invoke: () -> Unit) : Message() {
    override fun deliver() {
        invoke()
    }
}

data class EventMessage<T>(val invoke: (T) -> Unit, val data: T) : Message() {
    override fun deliver() {
        invoke(data)
    }
}

class MessageBuilder {
    @PublishedApi internal val list = mutableListOf<Message>()

    fun message(handler: () -> Unit) = apply {
        list.add(UnitMessage(handler))
    }

    fun <EventData> message(handler: (EventData) -> Unit, data: EventData) = apply {
        list.add(EventMessage(handler, data))
    }
}
