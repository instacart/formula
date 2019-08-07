package com.instacart.formula

/**
 * A [Stream] defines an asynchronous event(s).
 *
 * @param Input - Type of Input that is used to initialize a stream. Use [Unit] if stream doesn't need any input.
 * @param Output - Event type that the stream produces.
 */
interface Stream<Input, Output> {
    companion object {
        inline fun performOnCreate(crossinline action: () -> Unit): Stream<Unit, Unit> {
            return object : Stream<Unit, Unit> {
                override fun perform(input: Unit, onEvent: (Unit) -> Unit): Cancelation? {
                    action()
                    return null
                }
            }
        }

        inline fun <Input> performOnCreate(crossinline action: (Input) -> Unit): Stream<Input, Unit> {
            return object : Stream<Input, Unit> {
                override fun perform(input: Input, onEvent: (Unit) -> Unit): Cancelation? {
                    action(input)
                    return null
                }
            }
        }
    }

    fun perform(input: Input, onEvent: (Output) -> Unit): Cancelation?
}
