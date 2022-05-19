package com.instacart.formula.internal

import com.google.common.truth.Truth
import com.instacart.formula.subjects.DynamicStreamSubject
import com.instacart.formula.subjects.KeyFormula
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class ThreadCheckerTest {

    @Test
    fun `detects incorrect thread`() {
        val checker = ThreadChecker(KeyFormula())

        val latch = CountDownLatch(1)
        Thread {
            try {
                checker.check("error message")
                error("thread checker should fail")
            } catch (e: Exception) {
                Truth.assertThat(e.message).startsWith("com.instacart.formula.subjects.KeyFormula - error message")
                latch.countDown()
            }
        }.start()

        latch.await(1, TimeUnit.SECONDS)
    }
}