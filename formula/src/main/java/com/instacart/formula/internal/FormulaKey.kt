package com.instacart.formula.internal

import kotlin.reflect.KClass

/**
 * Defines a key for child [com.instacart.formula.ProcessorFormula].
 *
 * @param type A class type of child [com.instacart.formula.ProcessorFormula].
 * @param key An extra key parameter used to distinguish between the children of same [type].
 */
data class FormulaKey(
    val type: KClass<*>,
    val key: String
)
