package com.instacart.formula.test.messages


/**
 * Use this class as a listener to record and verify messages sent.
 */
class TestEventCallback<T> : (T) -> Unit {
    private val values: MutableList<T> = mutableListOf()

    override fun invoke(p1: T) {
        values.add(p1)
    }

    fun values(): List<T> = values
}
