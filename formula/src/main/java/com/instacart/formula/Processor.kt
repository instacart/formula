package com.instacart.formula

/**
 * TODO: docs
 */
interface Processor<Input, Output> {

    fun process(input: Input, onEvent: (Output) -> Unit)

    fun tearDown()
}
