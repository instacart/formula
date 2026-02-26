package com.instacart.formula.internal

internal interface DuplicateKeyLog {
    fun addLog(key: Any): Boolean
}
