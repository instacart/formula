package com.instacart.formula.lifecycle

internal interface DuplicateKeyLog {
    fun addLog(key: Any): Boolean
}
