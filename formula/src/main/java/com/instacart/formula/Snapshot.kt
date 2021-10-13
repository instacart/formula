package com.instacart.formula

/**
 * TODO: documentation
 */
interface Snapshot<out Input, State> {
    val input: Input
    val state: State
    val context: FormulaContext<State>
}