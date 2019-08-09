package com.instacart.formula

/**
 * A [Stream] defines an asynchronous event(s).
 *
 * @param Input - Type of Input that is used to initialize a stream. Use [Unit] if stream doesn't need any input.
 * @param Output - Event type that the stream produces.
 */
interface Stream<Input, Output> {
    companion object {

        fun <Input> effect(): Stream<Input, Input> {
            @Suppress("UNCHECKED_CAST")
            return EffectStream as Stream<Input, Input>
        }

        fun cancellationEffect(): Stream<Unit, Unit> {
            @Suppress("UNCHECKED_CAST")
            return CancellationEffectStream as Stream<Unit, Unit>
        }
    }

    fun perform(input: Input, onEvent: (Output) -> Unit): Cancelation?
}

/**
 * Triggers [onEvent] as soon as [perform] is called.
 */
internal object EffectStream : Stream<Any, Any> {
    override fun perform(input: Any, onEvent: (Any) -> Unit): Cancelation? {
        onEvent(input)
        return null
    }
}

/**
 * Triggers [onEvent] when [Formula] is removed.
 */
internal object CancellationEffectStream : Stream<Any, Any> {
    override fun perform(input: Any, onEvent: (Any) -> Unit): Cancelation? {
        return Cancelation {
            onEvent(input)
        }
    }
}


