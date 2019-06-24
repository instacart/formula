package com.instacart.formula

import kotlin.reflect.KClass

class Worker<InputT : Any, OutputT>(
    val key: Key,
    val input: InputT,
    val processor: Processor<InputT, OutputT>,
    onEvent: (OutputT) -> Unit
) {

    internal var handler: (OutputT) -> Unit = onEvent

    data class Key(
        val input: Any,
        val processorType: KClass<*>,
        val tag: String = ""
    )

    internal fun start() {
        processor.process(input) {
            handler.invoke(it)
        }
    }

    internal fun tearDown() {
        processor.tearDown()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Worker<*, *>

        if (key != other.key) return false

        return true
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }
}
