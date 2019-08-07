package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.test.test
import org.junit.Test

class FormulaRuntimeTest {

    @Test
    fun `multiple event updates`() {
        StreamFormula()
            .test()
            .renderModel { startListening() }
            .apply { formula.incrementEvents.triggerIncrement() }
            .apply { formula.incrementEvents.triggerIncrement() }
            .apply { formula.incrementEvents.triggerIncrement() }
            .apply {
                assertThat(values().map { it.state }).containsExactly(0, 0, 1, 2, 3)
            }
    }

    @Test
    fun `no state changes after event stream is removed`() {
        StreamFormula()
            .test()
            .renderModel { startListening() }
            .apply { formula.incrementEvents.triggerIncrement() }
            .renderModel { stopListening() }
            .apply { formula.incrementEvents.triggerIncrement() }
            .apply { formula.incrementEvents.triggerIncrement() }
            .apply {
                assertThat(values().map { it.state }).containsExactly(0, 0, 1, 1)
            }
    }

    @Test
    fun `each child event handler should be scoped to latest state`() {
        MultipleChildEvents
            .formula()
            .test()
            .renderModel { child.incrementAndOutput() }
            .renderModel { child.incrementAndOutput() }
            .renderModel { child.incrementAndOutput() }
            .renderModel { assertThat(state).isEqualTo(3) }
    }

    @Test
    fun `multiple child worker updates`() {
        ChildStreamEvents()
            .startListening()
            .incrementBy(3)
            .assertCurrentValue(3)
    }

    @Test
    fun `child worker is removed`() {
        ChildStreamEvents()
            .startListening()
            .incrementBy(2)
            .stopListening()
            .incrementBy(4)
            .assertCurrentValue(2)
    }

    @Test
    fun `parent removes child when child emits an output`() {
        ChildRemovedOnOutputEvent()
            .assertChildIsVisible(true)
            .closeByOutput()
            .assertChildIsVisible(false)
    }

    @Test
    fun `transition after no re-evaluation pass`() {
        val sideEffectService = SideEffectService()
        TransitionAfterNoEvaluationPass
            .formula(sideEffectService)
            .test()
            .renderModel { triggerSideEffect() }
            .renderModel { triggerSideEffect() }
            .assertRenderModelCount(1)
            .apply {
                assertThat(sideEffectService.invoked).isEqualTo(2)
            }
    }

    @Test
    fun `child transition after no re-evaluation pass`() {
        val sideEffectService = SideEffectService()
        ChildTransitionAfterNoEvaluationPass
            .formula(sideEffectService)
            .test()
            .renderModel { child.triggerSideEffect() }
            .renderModel { child.triggerSideEffect() }
            .assertRenderModelCount(1)
            .apply {
                assertThat(sideEffectService.invoked).isEqualTo(2)
            }
    }

    @Test
    fun `nested child transition after no re-evaluation pass`() {
        val sideEffectService = SideEffectService()
        NestedChildTransitionAfterNoEvaluationPass
            .formula(sideEffectService)
            .test()
            .renderModel { child.child.triggerSideEffect() }
            .renderModel { child.child.triggerSideEffect() }
            .assertRenderModelCount(1)
            .apply {
                assertThat(sideEffectService.invoked).isEqualTo(2)
            }
    }

    @Test
    fun `runtime emits output events`() {
        OutputFormula()
            .test()
            .renderModel { triggerOutput() }
            .assertOutputCount(1)
            .assertRenderModelCount(1) // no state change, so no re-evaluation
    }

    @Test
    fun `output after no re-evaluation pass`() {
        OutputFormula()
            .test()
            .renderModel { triggerOutput() }
            .renderModel { triggerOutput() }
            .assertOutputCount(2)
            .assertRenderModelCount(1)
    }

    @Test
    fun `child output with no parent state change`() {
        ChildOutputNoParentStateChange
            .formula()
            .test()
            .renderModel { child.triggerOutput() }
            .assertOutputCount(1)
            .assertRenderModelCount(1) // no state change, so no re-evaluation
    }

    @Test
    fun `child output with parent state change`() {
        ChildOutputWithParentStateChange
            .formula()
            .test()
            .renderModel { child.triggerOutput() }
            .assertOutputCount(1)
            .assertRenderModelCount(2)
    }

    @Test fun `side effect triggers parent state transition`() {
        SideEffectTriggersParentTransition
            .formula()
            .test()
            .renderModel { child.triggerSideEffect() }
            .renderModel {
                assertThat(count).isEqualTo(1)
            }
    }

    @Test
    fun `child state is reset after toggle`() {
        ChildStateResetAfterToggle
            .formula()
            .test()
            .renderModel { child!!.incrementAndOutput() }
            .renderModel { child!!.incrementAndOutput() }
            .renderModel { assertThat(child!!.state).isEqualTo(2) }
            .renderModel { toggleChild() }
            .renderModel { assertThat(child).isNull() }
            .renderModel { toggleChild() }
            .renderModel { assertThat(child!!.state).isEqualTo(0) }
    }

    @Test
    fun `multiple callbacks using the same render model`() {
        OutputFormula()
            .test()
            .renderModel {
                incrementAndOutput()
                incrementAndOutput()
                incrementAndOutput()
            }
            .renderModel {
                assertThat(state).isEqualTo(3)
            }
    }

    @Test
    fun `multiple event callbacks using the same render model`() {
        EventCallbackFormula().test()
            .renderModel {
                changeState("one")
                changeState("two")
                changeState("three")
            }
            .renderModel { assertThat(state).isEqualTo("three") }
    }

    @Test
    fun `using a removed child callback should do nothing`() {
        OptionalChildFormula(OutputFormula())
            .test()
            .renderModel {
                val cachedChild = child!!
                toggleChild()

                cachedChild.incrementAndOutput()
                cachedChild.incrementAndOutput()
                cachedChild.incrementAndOutput()
            }
            .renderModel { assertThat(child).isNull() }
    }

    @Test
    fun `callbacks are equal across render model changes`() {
        OutputFormula()
            .test()
            .renderModel { incrementAndOutput() }
            .renderModel { incrementAndOutput() }
            .assertRenderModelCount(3)
            .apply {
                assertThat(values().map { it.incrementAndOutput }.toSet()).hasSize(1)
            }
    }

    @Test
    fun `event callbacks are equal across render model changes`() {
        EventCallbackFormula().test()
            .renderModel {
                changeState("one")
                changeState("two")
            }
            .assertRenderModelCount(3)
            .apply {
                assertThat(values().map { it.changeState }.toSet()).hasSize(1)
            }
    }

    @Test
    fun `removed callback is disabled`() {
        OptionalCallbackFormula()
            .test()
            .renderModel {

                callback?.invoke()
                toggleCallback()
                callback?.invoke()
            }
            .apply {
                assertThat(values().map { it.state }).containsExactly(0, 1, 1)
            }
    }

    @Test
    fun `callbacks are not the same after removing then adding it again`() {
        OptionalCallbackFormula()
            .test()
            .renderModel {
                toggleCallback()
                toggleCallback()
            }
            .apply {
                assertThat(values().map { it.callback }.toSet()).hasSize(3)
            }
    }

    @Test
    fun `removed event callback is disabled`() {
        OptionalEventCallbackFormula()
            .test()
            .renderModel {
                callback?.invoke(1)
                toggleCallback()
                callback?.invoke(5)
            }
            .apply {
                assertThat(values().map { it.state }).containsExactly(0, 1, 1)
            }
    }

    @Test
    fun `event callbacks are not the same after removing then adding it again`() {
        OptionalEventCallbackFormula()
            .test()
            .renderModel {
                toggleCallback()
                toggleCallback()
            }
            .apply {
                assertThat(values().map { it.callback }.toSet()).hasSize(3)
            }
    }

    @Test
    fun `remove item from a list using a key block for each item`() {
        KeyUsingListFormula
            .test(items = listOf("one", "two", "three"))
            .renderModel {
                items[1].onDeleteSelected()
            }
            .renderModel { assertThat(items).hasSize(2) }
    }
}
