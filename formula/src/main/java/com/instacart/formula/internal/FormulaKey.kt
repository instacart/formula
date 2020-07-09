package com.instacart.formula.internal

import kotlin.reflect.KClass

internal data class FormulaKey(
    val type: KClass<*>,
    val key: Any?
)
