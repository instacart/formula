package com.instacart.formula

/**
 * Represents a basic reactive program that takes an [Input] and returns an [Output] over time. Take
 * a look at [Formula] for the implementation details.
 *
 * @param Input Data and callbacks that are provided by the parent.
 * @param Output Data and callbacks that are returned by this formula.
 */
interface IFormula<Input, Output> {

    /**
     * The actual implementation is deferred.
     *
     * @see Formula
     */
    fun implementation(): Formula<Input, *, Output>
}