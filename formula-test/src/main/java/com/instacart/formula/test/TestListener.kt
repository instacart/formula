package com.instacart.formula.test

import com.instacart.formula.Listener


/**
 * Use this class as a listener to record and verify messages sent.
 */
class TestListener<Event> : Listener<Event> {
    private val values: MutableList<Event> = mutableListOf()

    override fun invoke(p1: Event) {
        values.add(p1)
    }

    fun values(): List<Event> = values

    fun assertTimesCalled(times: Int) {
        val timesCalled = values.size
        assert(timesCalled == times) {
            "Expected: $times, was: $timesCalled"
        }
    }
}
