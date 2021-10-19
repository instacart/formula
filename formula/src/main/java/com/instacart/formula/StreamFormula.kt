package com.instacart.formula

/**
 * Converts [Stream] into a [Formula] which emits [initial value][initialValue]
 * until [stream][stream] produces a value. It will recreate and resubscribe to
 * the [Stream] whenever [Input] changes.
 */
abstract class StreamFormula<Input : Any, Output : Any> : IFormula<Input, Output> {

    /**
     * Initial value returned by this formula.
     */
    abstract fun initialValue(input: Input): Output

    /**
     * A factory function that takes an [Input] and constructs a [Stream] of type [Output].
     */
    abstract fun stream(input: Input): Stream<Output>

    // Implements the common API used by the runtime.
    private val implementation = object : Formula<Input, Output, Output>() {
        override fun initialState(input: Input) = initialValue(input)

        override fun Snapshot<Input, Output>.evaluate(): Evaluation<Output> {
            return Evaluation(
                output = state,
                updates = context.updates {
                    stream(input).onEvent {
                        transition(it)
                    }
                }
            )
        }

        override fun key(input: Input): Any = input
    }

    final override fun implementation(): Formula<Input, *, Output> = implementation
}
