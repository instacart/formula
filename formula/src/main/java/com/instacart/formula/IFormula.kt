package com.instacart.formula

import kotlin.reflect.KClass

/**
 * Represents a composable, reactive program that takes an [input][Input] and produces
 * an [output][Output]. Each formula can manage its own [internal state][Formula.initialState]
 * or be [stateless][StatelessFormula].
 *
 * @param Input A data class provided by the parent that contains data and event listeners. Input change
 * will trigger [Formula.onInputChanged] and [Formula.evaluate] to be called and new [Output] will
 * be created. Use [Unit] type when there is no input.
 *
 * @param Output A data class returned by this formula that contains data and event listeners. When
 * it is used to render UI, we call it a render model (Ex: ItemRenderModel).
 */
interface IFormula<in Input, out Output> {
    /**
     * [Formula] is the common API used internally by the runtime. This method allows us
     * to define the contract and hide the implementation details. We also override this
     * method to provide a fake implementation when writing tests.
     */
    val implementation: Formula<Input, *, Output>

    /**
     * Type is used in conjunction with [key] to uniquely identify a formula
     * instance. This method allows us to preserve the original identity of formula
     * when using delegation and composition.
     */
    fun type(): Class<*> = this::class.java

    /**
     * A unique identifier used to distinguish formulas of the same type. This can also
     * be used to [restart][Formula.initialState] formula when some input property changes.
     * ```
     * override fun key(input: ItemInput) = input.itemId
     * ```
     */
    fun key(input: Input): Any? = implementation.key(input)
}
