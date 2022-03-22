package com.instacart.formula

/**
 * Converts [Stream] into a [Formula] which emits [initial value][initialValue]
 * until [stream][stream] produces a value. It will recreate and resubscribe to
 * the [Stream] whenever [Input] changes.
 */
@Deprecated("Use ActionFormula directly.")
abstract class StreamFormula<Input : Any, Output : Any> : ActionFormula<Input, Output>() {

    /**
     * A factory function that takes an [Input] and constructs a [Stream] of type [Output].
     */
    abstract fun stream(input: Input): Stream<Output>

    final override fun action(input: Input): Action<Output> = stream(input)
}
