package com.instacart.formula.processor

import com.squareup.kotlinpoet.TypeName

/**
 * Defines class containing reduce methods.
 */
data class ReducerClass(
    val type: TypeName,
    val reduceMethods: List<ReduceMethod>,
    val effectType: TypeName
)
