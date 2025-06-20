package com.instacart.formula.plugin

sealed class FormulaError {

    /**
     * An unhandled error occurred during the execution of an action. By default,
     * we want actions to emit errors as part of event type and be handled
     * within the formula onEvent block.
     */
    data class ActionError(
        override val formula: Class<*>,
        override val error: Throwable,
    ): FormulaError()

    /**
     * An unhandled error occurred during formula run.
     */
    data class Unhandled(
        override val formula: Class<*>,
        override val error: Throwable,
    ): FormulaError()

    abstract val formula: Class<*>
    abstract val error: Throwable
}