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
        ChildTimer()
            .stepBy(seconds = 3)
            .assertTimeValues(
                "Time: 0",
                "Time: 1",
                "Time: 2",
                "Time: 3"
            )
    }

    @Test
    fun `child worker is removed`() {
        ChildTimer()
            .stepBy(seconds = 2)
            .resetTimer()
            .stepBy(seconds = 4)
            .assertTimeValues(
                "Time: 0",
                "Time: 1",
                "Time: 2",
                "Time: 0"
            )
    }

    @Test
    fun `child is removed through output`() {
        ChildTimer()
            .stepBy(seconds = 1)
            .close()
            .assertTimerIsVisible(true)
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
            .renderModel { assertThat(child!!.childState).isEqualTo(2) }
            .renderModel { toggleChild() }
            .renderModel { assertThat(child).isNull() }
            .renderModel { toggleChild() }
            .renderModel { assertThat(child!!.childState).isEqualTo(0) }
    }
}
