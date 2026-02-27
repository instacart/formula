package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.internal.ActionDelegate
import com.instacart.formula.plugin.FormulaError
import com.instacart.formula.plugin.Inspector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestScope
import org.junit.Test

class DeferredActionTest {

    private val fakeDelegate = object : ActionDelegate {
        override val scope: CoroutineScope = TestScope()
        override val formulaType: Class<*> = Any::class.java
        override val inspector: Inspector? = null
        override val onError: (FormulaError) -> Unit = {}
    }

    @Test
    fun `identity equality returns true`() {
        val callback = { _: Unit -> }
        val action = DeferredAction(key = "1", action = Action.onInit(), callback, fakeDelegate)
        assertThat(action.equals(action)).isTrue()
    }

    @Test
    fun `when class is different, equality returns false`() {
        val callback = { _: Unit -> }
        val action = DeferredAction(key = "1", action = Action.onInit(), callback, fakeDelegate)
        assertThat(action).isNotEqualTo("string")
    }

    @Test
    fun `when listeners are not equal, the actions are not equal`() {
        val callback = { _: Unit -> }
        val callback2 = { _: Unit -> }
        val action1 = DeferredAction(key = "1", action = Action.onInit(), callback, fakeDelegate)
        val action2 = DeferredAction(key = "1", action = Action.onInit(), callback2, fakeDelegate)

        assertThat(action1).isNotEqualTo(action2)
    }
}
