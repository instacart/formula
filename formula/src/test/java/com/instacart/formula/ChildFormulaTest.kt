package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class ChildFormulaTest {
    lateinit var child: ChildTimer

    @Before fun setup() {
        child = ChildTimer()
    }

    @Test fun `child updates`() {
        child.subject
            .apply {
                child.step(seconds = 3)
            }
            .apply {
                val timeValues = values().map { it.child!!.time }
                assertThat(timeValues).containsExactly(
                    "Time: 0",
                    "Time: 1",
                    "Time: 2",
                    "Time: 3"
                )
            }
    }

    @Test fun `child worker is removed`() {
       child
           .subject
            .apply {
                child.step(seconds = 2)

                values().last().child!!.onResetSelected()

                child.step(seconds = 4)
            }
            .apply {
                val timeValues = values().map { it.child!!.time }
                assertThat(timeValues).containsExactly(
                    "Time: 0",
                    "Time: 1",
                    "Time: 2",
                    "Time: 0"
                )
            }
    }

    @Test fun `child is removed through effects`() {
        child
            .subject
            .apply {
                child.step(seconds = 1)
            }
            .renderModel { child!!.onClose() }
            .renderModel { assertThat(child).isNull() }
    }
}
