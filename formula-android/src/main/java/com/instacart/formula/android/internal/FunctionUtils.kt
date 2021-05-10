package com.instacart.formula.android.internal

internal object FunctionUtils {
    fun <C> identity(): (C) -> C {
        return { it }
    }
}