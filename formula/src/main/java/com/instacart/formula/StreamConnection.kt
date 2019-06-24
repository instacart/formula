package com.instacart.formula

import com.instacart.formula.internal.StreamKey
import io.reactivex.disposables.Disposable

class StreamConnection<Input : Any, Output>(
    val key: StreamKey,
    val input: Input,
    val stream: Stream<Input, Output>,
    onEvent: (Output) -> Unit
) {

    internal var handler: (Output) -> Unit = onEvent
    internal var disposable: Disposable? = null

    internal fun start() {
        disposable = stream.subscribe(input) {
            handler.invoke(it)
        }
    }

    internal fun tearDown() {
        disposable?.dispose()
        disposable = null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StreamConnection<*, *>

        if (key != other.key) return false

        return true
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }
}
