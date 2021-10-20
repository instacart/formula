package com.instacart.formula

/**
 * A snapshot is a short-lived class that is used within [Formula.evaluate]. It provides
 * the [input], [state] and [context] that can be used during evaluation. Any time there
 * is a state change within the Formula hierarchy, a new snapshot will be generated and
 * [Formula.evaluate] will be called again.
 */
interface Snapshot<out Input, State> {

    /**
     * The current Formula input value passed by the parent.
     */
    val input: Input

    /**
     * The current Formula state value.
     */
    val state: State

    /**
     * Context is a short-lived object associated with the current evaluation. It should not be
     * used after evaluation has finished.
     */
    val context: FormulaContext<Input, State>
}