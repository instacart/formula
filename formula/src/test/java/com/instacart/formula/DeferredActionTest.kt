package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DeferredActionTest {

    @Test
    fun `identity equality returns true`() {
        val callback = { _: Unit -> }
        val action = DeferredAction(key = "1", action = Action.onInit(), callback)
        assertThat(action.equals(action)).isTrue()
    }

    @Test
    fun `when class is different, equality returns false`() {
        val callback = { _: Unit -> }
        val action = DeferredAction(key = "1", action = Action.onInit(), callback)
        assertThat(action).isNotEqualTo("string")
    }

    @Test
    fun `when listeners are not equal, the actions are not equal`() {
        val callback = { _: Unit -> }
        val callback2 = { _: Unit -> }
        val action1 = DeferredAction(key = "1", action = Action.onInit(), callback)
        val action2 = DeferredAction(key = "1", action = Action.onInit(), callback2)

        assertThat(action1).isNotEqualTo(action2)
    }
}