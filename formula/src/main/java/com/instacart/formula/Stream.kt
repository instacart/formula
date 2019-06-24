package com.instacart.formula

import io.reactivex.disposables.Disposable

/**
 * A [Stream] defines an asynchronous event(s).
 *
 * @param Input - Type of Input that is used to initialize a stream. Use [Unit] if stream doesn't need any input.
 * @param Output - Event type that the stream produces.
 */
interface Stream<Input, Output> {

    fun subscribe(input: Input, onEvent: (Output) -> Unit): Disposable
}
