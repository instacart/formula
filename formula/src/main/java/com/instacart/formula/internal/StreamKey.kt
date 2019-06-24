package com.instacart.formula.internal

import kotlin.reflect.KClass

/**
 * A way to ensure uniqueness and equality between [Stream]s.
 */
data class StreamKey(
    val input: Any,
    val processorType: KClass<*>,
    val tag: String = ""
)
