package com.instacart.formula

interface ParameterProvider<out Input, State> {

    /**
     * Current formula input.
     */
    val input: Input
    /**
     * Current formula state.
     */
    val state: State
}