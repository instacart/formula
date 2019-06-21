package com.instacart.formula

import io.reactivex.disposables.Disposable

abstract class RxProcessor<Input, Output> : Processor<Input, Output> {
    private var running: Disposable? = null

    final override fun process(input: Input, onEvent: (Output) -> Unit) {
        running = subscribe(input, onEvent)
    }

    final override fun tearDown() {
        running?.dispose()
        running = null
    }

    abstract fun subscribe(input: Input, onEvent: (Output) -> Unit): Disposable
}
