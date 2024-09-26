package com.instacart.formula.test

class TestCallback : () -> Unit {
    private var invocationCount: Int = 0

    override fun invoke() {
        invocationCount += 1
    }

    fun assertTimesCalled(times: Int) {
        if (invocationCount != times) {
            throw AssertionError("Expected: $times, was: $invocationCount")
        }
    }
}
