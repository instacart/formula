package com.instacart.formula

/**
 * Converts [Action] into a [Formula] which emits [initial value][initialValue]
 * until [action][action] produces a value. It will recreate and resubscribe to
 * the [Action] whenever [Input] changes.
 */
abstract class ActionFormula<Input : Any, Output : Any> : IFormula<Input, Output> {
    // Implements the common API used by the runtime.
    final override val implementation: Formula<Input, Output, Output> = object : Formula<Input, Output, Output>() {
        override fun initialState(input: Input) = initialValue(input)

        override fun Snapshot<Input, Output>.evaluate(): Evaluation<Output> {
            registerAction()
            return Evaluation(output = state)
        }

        override fun key(input: Input): Any = input

        private fun ActionBuilder<Input, Output>.registerAction() {
            val executionType = this@ActionFormula.executionType()
            this@ActionFormula.action(input).onEventWithExecutionType(executionType) {
                transition(it)
            }
        }
    }

    /**
     * Initial value returned by this formula.
     */
    abstract fun initialValue(input: Input): Output

    /**
     * A factory function that takes an [Input] and constructs a [Action] of type [Output].
     */
    abstract fun action(input: Input): Action<Output>

    /**
     * Transition execution type that will be used with this action.
     */
    open fun executionType(): Transition.ExecutionType? = null
}
