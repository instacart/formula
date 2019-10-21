package com.instacart.formula.test

class TestCallback : () -> Unit {
    private var invocationCount: Int = 0

    override fun invoke() {
        invocationCount += 1
    }

    fun assertTimesCalled(times: Int) {
        assert(invocationCount == times) {
            "Expected: $times, was: $invocationCount"
        }
    }
}
