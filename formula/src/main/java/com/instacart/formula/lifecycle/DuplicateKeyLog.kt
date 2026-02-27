package com.instacart.formula.lifecycle

interface DuplicateKeyLog {
    fun addLog(key: Any): Boolean
}
