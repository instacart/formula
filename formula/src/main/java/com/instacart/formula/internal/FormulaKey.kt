package com.instacart.formula.internal

import kotlin.reflect.KClass

internal data class FormulaKey(
    val scopeKey: Any?,
    val type: KClass<*>,
    val key: Any?
)
